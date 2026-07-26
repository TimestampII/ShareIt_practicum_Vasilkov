package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Тело запроса на создание - пользователь передаёт только текст запроса,
// requestor и created проставляются на сервере.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewItemRequestDto {
    private String description;
}
