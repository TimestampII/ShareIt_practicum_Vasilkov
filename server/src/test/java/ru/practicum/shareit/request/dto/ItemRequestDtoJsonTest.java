package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// DTO содержит LocalDateTime (created) - проверяем, что дата сериализуется
// в ожидаемом ISO-формате, а не как массив чисел (что бывает по умолчанию
// без правильно настроенного jackson-datatype-jsr310).
@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void serialize_shouldWriteAllFieldsAndDateAsIsoString() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 7, 26, 12, 30, 0);
        ItemAnswerDto answer = new ItemAnswerDto(10L, "Дрель", 20L);
        ItemRequestDto dto = new ItemRequestDto(1L, "Нужна дрель", created, List.of(answer));

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна дрель");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-07-26T12:30:00");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(20);
    }

    @Test
    void serialize_shouldWriteEmptyItemsArray_whenNoAnswers() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(1L, "Нужна дрель", LocalDateTime.now(), List.of());

        var result = json.write(dto);

        assertThat(result).extractingJsonPathArrayValue("$.items").isEmpty();
    }
}