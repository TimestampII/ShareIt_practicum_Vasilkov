package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewBookingDto {
    @NotNull(message = "Не указана вещь для бронирования")
    private Long itemId;

    @NotNull(message = "Не указана дата начала бронирования")
    @FutureOrPresent(message = "Дата начала бронирования не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(message = "Не указана дата окончания бронирования")
    @Future(message = "Дата окончания бронирования должна быть в будущем")
    private LocalDateTime end;

    // Проверка "end после start" не требует обращения к БД,
    // поэтому по ТЗ её тоже делаем на gateway, а не только на сервере.
    @AssertTrue(message = "Дата окончания бронирования должна быть позже даты начала")
    private boolean isEndAfterStart() {
        return start == null || end == null || end.isAfter(start);
    }
}
