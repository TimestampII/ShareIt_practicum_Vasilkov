package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Краткое представление вещи, добавленной в ответ на запрос:
// id вещи, название, id владельца (по ТЗ).
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemAnswerDto {
    private Long id;
    private String name;
    private Long ownerId;
}
