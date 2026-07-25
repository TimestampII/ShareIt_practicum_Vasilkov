package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.ItemShortDto;
import ru.practicum.shareit.booking.dto.UserShortDto;

public class BookingMapper {

    private BookingMapper() {
    }

    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        ItemShortDto itemShortDto = new ItemShortDto(
                booking.getItem().getId(),
                booking.getItem().getName()
        );
        UserShortDto bookerShortDto = new UserShortDto(booking.getBooker().getId());

        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                itemShortDto,
                bookerShortDto
        );
    }
}
