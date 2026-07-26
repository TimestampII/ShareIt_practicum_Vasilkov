package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Краткое представление вещи внутри ответа о бронировании -
// клиенту не нужен весь Item целиком, только id и название.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemShortDto {
    private Long id;
    private String name;
}
