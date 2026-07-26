package ru.practicum.shareit.item;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

public class ItemClient extends BaseClient {

    public ItemClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> create(long ownerId, NewItemDto newItemDto) {
        return post("", ownerId, newItemDto);
    }

    public ResponseEntity<Object> update(long ownerId, Long itemId, UpdateItemDto updateItemDto) {
        return patch("/" + itemId, ownerId, updateItemDto);
    }

    public ResponseEntity<Object> getById(long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getAllByOwner(long ownerId) {
        return get("", ownerId);
    }

    public ResponseEntity<Object> search(String text) {
        Map<String, Object> parameters = Map.of("text", text);
        return get("/search?text={text}", null, parameters);
    }

    public ResponseEntity<Object> addComment(long userId, Long itemId, NewCommentDto newCommentDto) {
        return post("/" + itemId + "/comment", userId, newCommentDto);
    }
}