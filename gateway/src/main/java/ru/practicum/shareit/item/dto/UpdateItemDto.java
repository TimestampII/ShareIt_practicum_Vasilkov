package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Частичное обновление - все поля необязательны, пустые/null не трогают существующее значение.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateItemDto {
    private String name;
    private String description;
    private Boolean available;
}
