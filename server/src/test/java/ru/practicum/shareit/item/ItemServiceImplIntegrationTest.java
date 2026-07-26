package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Owner", "owner@mail.com"));
        otherUser = userRepository.save(new User(null, "Other", "other@mail.com"));
    }

    @Test
    void create_shouldSaveItemWithoutRequest() {
        ItemDto newItemDto = new ItemDto(null, "Дрель", "Мощная дрель", true, null);

        ItemDto result = itemService.create(owner.getId(), newItemDto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getRequestId()).isNull();
    }

    @Test
    void create_shouldLinkItemToRequest_whenRequestIdProvided() {
        ItemRequest request = itemRequestRepository.save(
                new ItemRequest(null, "Нужна дрель", otherUser, LocalDateTime.now()));

        ItemDto newItemDto = new ItemDto(null, "Дрель", "Мощная дрель", true, request.getId());
        ItemDto result = itemService.create(owner.getId(), newItemDto);

        assertThat(result.getRequestId()).isEqualTo(request.getId());
    }

    @Test
    void create_shouldThrowNotFoundException_whenRequestIdNotExists() {
        long unknownRequestId = 999_999L;
        ItemDto newItemDto = new ItemDto(null, "Дрель", "Мощная дрель", true, unknownRequestId);

        assertThrows(NotFoundException.class, () -> itemService.create(owner.getId(), newItemDto));
    }

    @Test
    void create_shouldThrowValidationException_whenNameBlank() {
        ItemDto newItemDto = new ItemDto(null, "  ", "Мощная дрель", true, null);

        assertThrows(ValidationException.class, () -> itemService.create(owner.getId(), newItemDto));
    }

    @Test
    void update_shouldChangeOnlyProvidedFields() {
        ItemDto created = itemService.create(owner.getId(),
                new ItemDto(null, "Дрель", "Мощная дрель", true, null));

        ItemDto patch = new ItemDto(null, "Новая дрель", null, null, null);
        ItemDto updated = itemService.update(owner.getId(), created.getId(), patch);

        assertThat(updated.getName()).isEqualTo("Новая дрель");
        assertThat(updated.getDescription()).isEqualTo("Мощная дрель");
        assertThat(updated.getAvailable()).isTrue();
    }

    @Test
    void update_shouldThrowNotFoundException_whenNotOwner() {
        ItemDto created = itemService.create(owner.getId(),
                new ItemDto(null, "Дрель", "Мощная дрель", true, null));
        ItemDto patch = new ItemDto(null, "Чужая правка", null, null, null);

        assertThrows(NotFoundException.class,
                () -> itemService.update(otherUser.getId(), created.getId(), patch));
    }

    @Test
    void getById_shouldReturnItemWithoutBookingDates_forNonOwner() {
        ItemDto created = itemService.create(owner.getId(),
                new ItemDto(null, "Дрель", "Мощная дрель", true, null));

        ItemWithBookingsDto result = itemService.getById(otherUser.getId(), created.getId());

        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void getAllByOwner_shouldReturnOnlyOwnersItems() {
        itemService.create(owner.getId(), new ItemDto(null, "Дрель", "Описание", true, null));
        itemService.create(otherUser.getId(), new ItemDto(null, "Шуруповёрт", "Описание", true, null));

        List<ItemWithBookingsDto> ownerItems = itemService.getAllByOwner(owner.getId());

        assertThat(ownerItems).hasSize(1);
        assertThat(ownerItems.get(0).getName()).isEqualTo("Дрель");
    }

    @Test
    void search_shouldReturnOnlyAvailableItemsMatchingText() {
        itemService.create(owner.getId(), new ItemDto(null, "Дрель Bosch", "Ударная", true, null));
        itemService.create(owner.getId(), new ItemDto(null, "Дрель старая", "Не работает", false, null));
        itemService.create(owner.getId(), new ItemDto(null, "Молоток", "Обычный", true, null));

        List<ItemDto> found = itemService.search("дрель");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Дрель Bosch");
    }

    @Test
    void search_shouldReturnEmptyList_whenTextBlank() {
        assertThat(itemService.search("")).isEmpty();
        assertThat(itemService.search(null)).isEmpty();
    }
}