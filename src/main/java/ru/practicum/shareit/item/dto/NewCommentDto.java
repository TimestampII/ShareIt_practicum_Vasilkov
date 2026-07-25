package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// То, что присылает клиент при добавлении отзыва - только текст.
// author и created определяются на сервере, а не берутся из запроса.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCommentDto {
    private String text;
}
