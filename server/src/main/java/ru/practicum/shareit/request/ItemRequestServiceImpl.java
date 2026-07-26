package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto create(Long userId, NewItemRequestDto newItemRequestDto) {
        User requestor = getUserOrThrow(userId);

        if (newItemRequestDto.getDescription() == null || newItemRequestDto.getDescription().isBlank()) {
            throw new ValidationException("Текст запроса не может быть пустым");
        }

        ItemRequest request = ItemRequestMapper.toItemRequest(newItemRequestDto.getDescription(), requestor);
        ItemRequest saved = itemRequestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(saved, List.of());
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        getUserOrThrow(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(userId);
        return toDtosWithAnswers(requests);
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId) {
        getUserOrThrow(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestor_IdNotOrderByCreatedDesc(userId);
        return toDtosWithAnswers(requests);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        getUserOrThrow(userId);
        ItemRequest request = getRequestOrThrow(requestId);
        List<Item> items = itemRepository.findAllByRequest_Id(requestId);
        return ItemRequestMapper.toItemRequestDto(request, items);
    }

    private List<ItemRequestDto> toDtosWithAnswers(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        List<Item> allAnswers = itemRepository.findAllByRequest_IdIn(requestIds);
        Map<Long, List<Item>> answersByRequestId = allAnswers.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request, answersByRequestId.getOrDefault(request.getId(), List.of())))
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private ItemRequest getRequestOrThrow(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));
    }
}
