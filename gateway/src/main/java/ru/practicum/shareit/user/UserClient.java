package ru.practicum.shareit.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;

public class UserClient extends BaseClient {

    public UserClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> create(NewUserDto newUserDto) {
        return post("", newUserDto);
    }

    public ResponseEntity<Object> update(Long userId, UpdateUserDto updateUserDto) {
        return patch("/" + userId, updateUserDto);
    }

    public ResponseEntity<Object> getById(Long userId) {
        return get("/" + userId);
    }

    public ResponseEntity<Object> getAll() {
        return get("");
    }

    public ResponseEntity<Object> delete(Long userId) {
        return delete("/" + userId);
    }
}