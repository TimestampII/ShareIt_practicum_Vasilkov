package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

// UserDto - простое DTO без собственной логики форматирования,
// но проверяем базовую сериализацию/десериализацию на случай будущих изменений
// (например, добавления @JsonIgnore на пароль или другого чувствительного поля).
@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void serialize_shouldWriteAllFields() throws Exception {
        UserDto dto = new UserDto(1L, "Иван", "ivan@mail.com");

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Иван");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("ivan@mail.com");
    }

    @Test
    void deserialize_shouldReadAllFields() throws Exception {
        String content = "{\"id\":1,\"name\":\"Иван\",\"email\":\"ivan@mail.com\"}";

        UserDto dto = json.parse(content).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Иван");
        assertThat(dto.getEmail()).isEqualTo("ivan@mail.com");
    }

    @Test
    void deserialize_shouldAllowMissingId_forCreateRequest() throws Exception {
        String content = "{\"name\":\"Иван\",\"email\":\"ivan@mail.com\"}";

        UserDto dto = json.parse(content).getObject();

        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isEqualTo("Иван");
    }
}