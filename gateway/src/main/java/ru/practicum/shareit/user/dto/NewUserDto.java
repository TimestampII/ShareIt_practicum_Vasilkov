package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Тело запроса на создание пользователя - оба поля обязательны,
// email должен быть похож на настоящий адрес почты.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewUserDto {
    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email имеет некорректный формат")
    private String email;
}
