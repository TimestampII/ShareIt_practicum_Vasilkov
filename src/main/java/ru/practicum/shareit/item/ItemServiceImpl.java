package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = getUserOrThrow(ownerId);

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание вещи не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности вещи должен быть указан");
        }

        Item item = ItemMapper.toItem(itemDto, owner);
        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        getUserOrThrow(ownerId);
        Item existing = getItemOrThrow(itemId);
        if (!existing.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException(
                    "Пользователь " + ownerId + " не является владельцем вещи " + itemId);
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existing.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existing.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }
        Item updated = itemRepository.save(existing);
        return ItemMapper.toItemDto(updated);
    }

    @Override
    public ItemWithBookingsDto getById(Long userId, Long itemId) {
        Item item = getItemOrThrow(itemId);
        List<Comment> comments = commentRepository.findByItem_Id(itemId);

        // Даты бронирования показываем только владельцу вещи -
        // остальным пользователям они не нужны и могут раскрывать чужие данные аренды
        boolean isOwner = item.getOwner().getId().equals(userId);
        Booking lastBooking = null;
        Booking nextBooking = null;
        if (isOwner) {
            List<Booking> approvedBookings = bookingRepository
                    .findByItem_IdAndStatusOrderByStartAsc(itemId, BookingStatus.APPROVED);
            LocalDateTime now = LocalDateTime.now();
            lastBooking = findLastBooking(approvedBookings, now);
            nextBooking = findNextBooking(approvedBookings, now);
        }

        return ItemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemWithBookingsDto> getAllByOwner(Long ownerId) {
        List<Item> items = itemRepository.findAllByOwnerId(ownerId);
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        // Один запрос на все бронирования и один на все комментарии владельца -
        // вместо запроса на каждую вещь отдельно (см. заметку ментора про N+1)
        List<Booking> allBookings = bookingRepository
                .findByItem_IdInAndStatusOrderByStartAsc(itemIds, BookingStatus.APPROVED);
        Map<Long, List<Booking>> bookingsByItemId = allBookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        List<Comment> allComments = commentRepository.findByItem_IdIn(itemIds);
        Map<Long, List<Comment>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    List<Booking> itemBookings = bookingsByItemId.getOrDefault(item.getId(), List.of());
                    Booking lastBooking = findLastBooking(itemBookings, now);
                    Booking nextBooking = findNextBooking(itemBookings, now);
                    List<Comment> itemComments = commentsByItemId.getOrDefault(item.getId(), List.of());
                    return ItemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, itemComments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, NewCommentDto newCommentDto) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        if (newCommentDto.getText() == null || newCommentDto.getText().isBlank()) {
            throw new ValidationException("Текст отзыва не может быть пустым");
        }

        boolean tookItem = bookingRepository.existsByBooker_IdAndItem_IdAndStatusAndEndBefore(
                userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());
        if (!tookItem) {
            throw new ValidationException(
                    "Оставить отзыв может только пользователь, который брал вещь в аренду " +
                            "и срок аренды уже закончился");
        }

        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toCommentDto(saved);
    }

    // Последнее (ближайшее к текущему моменту) бронирование, которое уже началось
    private Booking findLastBooking(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> !b.getStart().isAfter(now))
                .max(Comparator.comparing(Booking::getStart))
                .orElse(null);
    }

    // Ближайшее будущее бронирование
    private Booking findNextBooking(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .orElse(null);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }
}
