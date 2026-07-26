package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// При редактировании оба поля необязательны (частичное обновление),
// но если email указан, он должен быть в корректном формате.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDto {
    private String name;

    @Email(message = "Email имеет некорректный формат")
    private String email;
}
