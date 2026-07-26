package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Интеграционный тест: поднимаем реальный контекст с H2 (test-профиль),
// проверяем взаимодействие сервиса с настоящими репозиториями.
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User requestor;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requestor = userRepository.save(new User(null, "Requestor", "requestor@mail.com"));
        otherUser = userRepository.save(new User(null, "Other", "other@mail.com"));
    }

    @Test
    void create_shouldSaveRequestAndReturnDtoWithEmptyItems() {
        NewItemRequestDto newRequestDto = new NewItemRequestDto("Нужна дрель");

        ItemRequestDto result = itemRequestService.create(requestor.getId(), newRequestDto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();
        assertThat(itemRequestRepository.findById(result.getId())).isPresent();
    }

    @Test
    void create_shouldThrowValidationException_whenDescriptionBlank() {
        NewItemRequestDto blankDto = new NewItemRequestDto("   ");

        assertThrows(ValidationException.class,
                () -> itemRequestService.create(requestor.getId(), blankDto));
    }

    @Test
    void create_shouldThrowNotFoundException_whenUserNotExists() {
        NewItemRequestDto newRequestDto = new NewItemRequestDto("Нужна дрель");
        long unknownUserId = requestor.getId() + otherUser.getId() + 1000;

        assertThrows(NotFoundException.class,
                () -> itemRequestService.create(unknownUserId, newRequestDto));
    }

    @Test
    void getOwn_shouldReturnOwnRequestsOrderedByCreatedDesc() throws InterruptedException {
        ItemRequestDto first = itemRequestService.create(requestor.getId(), new NewItemRequestDto("Первый запрос"));
        Thread.sleep(10); // гарантируем разный created для проверки сортировки
        ItemRequestDto second = itemRequestService.create(requestor.getId(), new NewItemRequestDto("Второй запрос"));

        List<ItemRequestDto> ownRequests = itemRequestService.getOwn(requestor.getId());

        assertThat(ownRequests).hasSize(2);
        assertThat(ownRequests.get(0).getId()).isEqualTo(second.getId());
        assertThat(ownRequests.get(1).getId()).isEqualTo(first.getId());
    }

    @Test
    void getOwn_shouldIncludeItemsAnsweringRequest() {
        ItemRequestDto request = itemRequestService.create(requestor.getId(), new NewItemRequestDto("Нужна дрель"));
        ItemRequest savedRequest = itemRequestRepository.findById(request.getId()).orElseThrow();

        Item answer = new Item(null, "Дрель", "Мощная дрель", true, otherUser, savedRequest);
        itemRepository.save(answer);

        List<ItemRequestDto> ownRequests = itemRequestService.getOwn(requestor.getId());

        assertThat(ownRequests).hasSize(1);
        assertThat(ownRequests.get(0).getItems()).hasSize(1);
        assertThat(ownRequests.get(0).getItems().get(0).getName()).isEqualTo("Дрель");
        assertThat(ownRequests.get(0).getItems().get(0).getOwnerId()).isEqualTo(otherUser.getId());
    }

    @Test
    void getAll_shouldReturnOtherUsersRequestsExcludingOwn() {
        itemRequestService.create(requestor.getId(), new NewItemRequestDto("Мой запрос"));
        ItemRequestDto othersRequest = itemRequestService.create(otherUser.getId(), new NewItemRequestDto("Чужой запрос"));

        List<ItemRequestDto> allForRequestor = itemRequestService.getAll(requestor.getId());

        assertThat(allForRequestor).hasSize(1);
        assertThat(allForRequestor.get(0).getId()).isEqualTo(othersRequest.getId());
    }

    @Test
    void getById_shouldReturnRequestWithItems_forAnyUser() {
        ItemRequestDto request = itemRequestService.create(requestor.getId(), new NewItemRequestDto("Нужна дрель"));

        // Запрашивает НЕ автор запроса - по ТЗ доступ разрешён любому пользователю
        ItemRequestDto result = itemRequestService.getById(otherUser.getId(), request.getId());

        assertThat(result.getId()).isEqualTo(request.getId());
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
    }

    @Test
    void getById_shouldThrowNotFoundException_whenRequestNotExists() {
        long unknownRequestId = 999_999L;

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getById(requestor.getId(), unknownRequestId));
    }
}