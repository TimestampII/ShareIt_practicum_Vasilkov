package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// То, что приходит от клиента при создании бронирования.
// Отдельный класс от ответа: клиент не присылает id, статус, объекты item/booker целиком.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewBookingDto {
    private Long itemId;
    private LocalDateTime start;
    private LocalDateTime end;
}
