package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item availableItem;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Owner", "owner@mail.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@mail.com"));
        availableItem = itemRepository.save(
                new Item(null, "Дрель", "Мощная дрель", true, owner, null));
    }

    @Test
    void create_shouldSaveBookingWithWaitingStatus() {
        NewBookingDto newBookingDto = new NewBookingDto(
                availableItem.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        BookingResponseDto result = bookingService.create(booker.getId(), newBookingDto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getItem().getId()).isEqualTo(availableItem.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void create_shouldThrowNotFoundException_whenBookingOwnItem() {
        NewBookingDto newBookingDto = new NewBookingDto(
                availableItem.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> bookingService.create(owner.getId(), newBookingDto));
    }

    @Test
    void create_shouldThrowValidationException_whenItemNotAvailable() {
        Item unavailableItem = itemRepository.save(
                new Item(null, "Старая дрель", "Сломана", false, owner, null));
        NewBookingDto newBookingDto = new NewBookingDto(
                unavailableItem.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThrows(ValidationException.class, () -> bookingService.create(booker.getId(), newBookingDto));
    }

    @Test
    void create_shouldThrowValidationException_whenStartAfterEnd() {
        NewBookingDto newBookingDto = new NewBookingDto(
                availableItem.getId(), LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class, () -> bookingService.create(booker.getId(), newBookingDto));
    }

    @Test
    void approve_shouldSetApprovedStatus_whenOwnerApproves() {
        BookingResponseDto created = bookingService.create(booker.getId(), new NewBookingDto(
                availableItem.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        BookingResponseDto approved = bookingService.approve(owner.getId(), created.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approve_shouldSetRejectedStatus_whenOwnerRejects() {
        BookingResponseDto created = bookingService.create(booker.getId(), new NewBookingDto(
                availableItem.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        BookingResponseDto rejected = bookingService.approve(owner.getId(), created.getId(), false);

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approve_shouldThrowForbiddenException_whenNotOwner() {
        BookingResponseDto created = bookingService.create(booker.getId(), new NewBookingDto(
                availableItem.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        assertThrows(ForbiddenException.class, () -> bookingService.approve(booker.getId(), created.getId(), true));
    }

    @Test
    void getById_shouldReturnBooking_forBookerOrOwner() {
        BookingResponseDto created = bookingService.create(booker.getId(), new NewBookingDto(
                availableItem.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        assertThat(bookingService.getById(booker.getId(), created.getId()).getId()).isEqualTo(created.getId());
        assertThat(bookingService.getById(owner.getId(), created.getId()).getId()).isEqualTo(created.getId());
    }

    @Test
    void getById_shouldThrowForbiddenException_forUnrelatedUser() {
        User stranger = userRepository.save(new User(null, "Stranger", "stranger@mail.com"));
        BookingResponseDto created = bookingService.create(booker.getId(), new NewBookingDto(
                availableItem.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        assertThrows(ForbiddenException.class, () -> bookingService.getById(stranger.getId(), created.getId()));
    }

    @Test
    void getAllByBooker_shouldReturnOwnBookingsOrderedByStartDesc() {
        bookingService.create(booker.getId(), new NewBookingDto(
                availableItem.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));
        Item secondItem = itemRepository.save(new Item(null, "Шуруповёрт", "Описание", true, owner, null));
        bookingService.create(booker.getId(), new NewBookingDto(
                secondItem.getId(), LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4)));

        List<BookingResponseDto> result = bookingService.getAllByBooker(booker.getId(), BookingState.ALL);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStart()).isAfter(result.get(1).getStart());
    }

    @Test
    void getAllByOwner_shouldReturnBookingsForOwnersItems() {
        bookingService.create(booker.getId(), new NewBookingDto(
                availableItem.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        List<BookingResponseDto> result = bookingService.getAllByOwner(owner.getId(), BookingState.ALL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItem().getId()).isEqualTo(availableItem.getId());
    }

    @Test
    void getAllByOwner_shouldThrowNotFoundException_whenOwnerNotExists() {
        long unknownOwnerId = owner.getId() + booker.getId() + 1000;

        assertThrows(NotFoundException.class, () -> bookingService.getAllByOwner(unknownOwnerId, BookingState.ALL));
    }
}