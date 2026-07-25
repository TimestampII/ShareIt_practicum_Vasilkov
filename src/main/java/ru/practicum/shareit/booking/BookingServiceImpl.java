package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private Map<BookingState, BookingStateFetchStrategy> bookerStrategies;
    private Map<BookingState, BookingStateFetchStrategy> ownerStrategies;

    @PostConstruct
    private void initStrategies() {
        bookerStrategies = Map.of(
                BookingState.ALL,
                (bookerId, now) -> bookingRepository.findByBooker_IdOrderByStartDesc(bookerId),
                BookingState.CURRENT,
                (bookerId, now) -> bookingRepository
                        .findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(bookerId, now, now),
                BookingState.PAST,
                (bookerId, now) -> bookingRepository.findByBooker_IdAndEndBeforeOrderByStartDesc(bookerId, now),
                BookingState.FUTURE,
                (bookerId, now) -> bookingRepository.findByBooker_IdAndStartAfterOrderByStartDesc(bookerId, now),
                BookingState.WAITING,
                (bookerId, now) -> bookingRepository
                        .findByBooker_IdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING),
                BookingState.REJECTED,
                (bookerId, now) -> bookingRepository
                        .findByBooker_IdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED)
        );

        ownerStrategies = Map.of(
                BookingState.ALL,
                (ownerId, now) -> bookingRepository.findByItem_Owner_IdOrderByStartDesc(ownerId),
                BookingState.CURRENT,
                (ownerId, now) -> bookingRepository
                        .findByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId, now, now),
                BookingState.PAST,
                (ownerId, now) -> bookingRepository.findByItem_Owner_IdAndEndBeforeOrderByStartDesc(ownerId, now),
                BookingState.FUTURE,
                (ownerId, now) -> bookingRepository.findByItem_Owner_IdAndStartAfterOrderByStartDesc(ownerId, now),
                BookingState.WAITING,
                (ownerId, now) -> bookingRepository
                        .findByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING),
                BookingState.REJECTED,
                (ownerId, now) -> bookingRepository
                        .findByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED)
        );
    }

    @Override
    public BookingResponseDto create(Long bookerId, NewBookingDto newBookingDto) {
        User booker = getUserOrThrow(bookerId);
        Item item = getItemOrThrow(newBookingDto.getItemId());

        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("Нельзя забронировать собственную вещь");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        validateDates(newBookingDto.getStart(), newBookingDto.getEnd());

        Booking booking = new Booking();
        booking.setStart(newBookingDto.getStart());
        booking.setEnd(newBookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(saved);
    }

    @Override
    public BookingResponseDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException(
                    "Подтвердить бронирование может только владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException(
                    "Решение по бронированию уже принято: " + booking.getStatus());
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(saved);
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new ForbiddenException(
                    "Доступ к бронированию есть только у автора или владельца вещи");
        }
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBooker(Long bookerId, BookingState state) {
        getUserOrThrow(bookerId);
        List<Booking> bookings = bookerStrategies.get(state).fetch(bookerId, LocalDateTime.now());
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long ownerId, BookingState state) {
        getUserOrThrow(ownerId);
        List<Booking> bookings = ownerStrategies.get(state).fetch(ownerId, LocalDateTime.now());
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            throw new ValidationException("Дата начала бронирования обязательна");
        }
        if (end == null) {
            throw new ValidationException("Дата окончания бронирования обязательна");
        }
        if (!start.isBefore(end)) {
            throw new ValidationException("Дата начала должна быть раньше даты окончания");
        }
        LocalDateTime now = LocalDateTime.now();
        if (start.isBefore(now)) {
            throw new ValidationException("Дата начала не может быть в прошлом");
        }
        if (end.isBefore(now)) {
            throw new ValidationException("Дата окончания не может быть в прошлом");
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));
    }
}
