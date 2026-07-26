package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// Проверяем сериализацию enum-статуса, вложенных ItemShortDto/UserShortDto
// и формат дат start/end.
@JsonTest
class BookingResponseDtoJsonTest {

    @Autowired
    private JacksonTester<BookingResponseDto> json;

    @Test
    void serialize_shouldWriteStatusAsPlainEnumName() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 8, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 2, 10, 0, 0);
        BookingResponseDto dto = new BookingResponseDto(
                1L, start, end, BookingStatus.APPROVED,
                new ItemShortDto(10L, "Дрель"), new UserShortDto(20L));

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-08-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-08-02T10:00:00");
    }

    @Test
    void serialize_shouldWriteNestedItemAndBooker() throws Exception {
        BookingResponseDto dto = new BookingResponseDto(
                1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1), BookingStatus.WAITING,
                new ItemShortDto(10L, "Дрель"), new UserShortDto(20L));

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(20);
    }

    @Test
    void serialize_shouldWriteAllStatusValues() throws Exception {
        for (BookingStatus status : BookingStatus.values()) {
            BookingResponseDto dto = new BookingResponseDto(
                    1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1), status,
                    new ItemShortDto(1L, "Вещь"), new UserShortDto(1L));

            var result = json.write(dto);

            assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo(status.name());
        }
    }
}