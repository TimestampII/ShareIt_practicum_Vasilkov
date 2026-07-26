package ru.practicum.shareit.request;

import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.request.dto.ItemAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    private ItemRequestMapper() {
    }

    public static ItemRequest toItemRequest(String description, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request, List<Item> items) {
        List<ItemAnswerDto> answers = items.stream()
                .map(ItemRequestMapper::toItemAnswerDto)
                .collect(Collectors.toList());
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                answers
        );
    }

    private static ItemAnswerDto toItemAnswerDto(Item item) {
        return new ItemAnswerDto(item.getId(), item.getName(), item.getOwner().getId());
    }
}
