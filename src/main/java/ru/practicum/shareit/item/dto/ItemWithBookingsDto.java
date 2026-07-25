package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Расширенная версия ItemDto для GET /items и GET /items/{itemId}:
// добавляет даты последнего/ближайшего бронирования и список отзывов.
// ItemDto (базовый) остаётся для POST/PATCH, где эти поля не нужны и не задаются клиентом.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemWithBookingsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
    private List<CommentDto> comments;
}
