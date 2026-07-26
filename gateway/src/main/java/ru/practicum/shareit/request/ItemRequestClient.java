package ru.practicum.shareit.request;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

public class ItemRequestClient extends BaseClient {

    public ItemRequestClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> create(long userId, NewItemRequestDto newItemRequestDto) {
        return post("", userId, newItemRequestDto);
    }

    public ResponseEntity<Object> getOwn(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAll(long userId) {
        return get("/all", userId);
    }

    public ResponseEntity<Object> getById(long userId, Long requestId) {
        return get("/" + requestId, userId);
    }
}