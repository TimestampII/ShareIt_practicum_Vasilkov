package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Краткое представление бронирования внутри карточки вещи -
// владельцу достаточно знать id брони и id арендатора.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingShortDto {
    private Long id;
    private Long bookerId;
}
