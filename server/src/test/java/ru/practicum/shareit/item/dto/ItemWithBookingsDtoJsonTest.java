package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Комментарии внутри содержат LocalDateTime (created) - проверяем формат даты
// и корректную сериализацию вложенных объектов lastBooking/nextBooking.
@JsonTest
class ItemWithBookingsDtoJsonTest {

    @Autowired
    private JacksonTester<ItemWithBookingsDto> json;
    @Autowired
    private JacksonTester<CommentDto> commentJson;

    @Test
    void serialize_shouldWriteNullBookings_whenNotOwner() throws Exception {
        ItemWithBookingsDto dto = new ItemWithBookingsDto(
                1L, "Дрель", "Описание", true, null, null, List.of());

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        // Поле присутствует в JSON, но со значением null - не используем
        // hasJsonPathValue (он трактует null как "нет значения" и падает),
        // просто извлекаем значение и проверяем, что оно null.
        assertThat(result).extractingJsonPathValue("$.lastBooking").isNull();
        assertThat(result).extractingJsonPathValue("$.nextBooking").isNull();
    }

    @Test
    void serialize_shouldWriteBookingIds_whenPresent() throws Exception {
        BookingShortDto lastBooking = new BookingShortDto(10L, 20L);
        ItemWithBookingsDto dto = new ItemWithBookingsDto(
                1L, "Дрель", "Описание", true, lastBooking, null, List.of());

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(10);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(20);
    }

    @Test
    void commentDto_shouldWriteCreatedAsIsoDate() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 7, 26, 15, 0, 0);
        CommentDto comment = new CommentDto(1L, "Отлично!", "Автор", created);

        var result = commentJson.write(comment);

        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-07-26T15:00:00");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Автор");
    }
}