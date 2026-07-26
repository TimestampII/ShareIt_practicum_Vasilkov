package ru.practicum.shareit.booking;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.client.BaseClient;

public class BookingClient extends BaseClient {

    public BookingClient(RestTemplate rest) {
        super(rest);
    }

    public ResponseEntity<Object> create(long bookerId, NewBookingDto newBookingDto) {
        return post("", bookerId, newBookingDto);
    }

    public ResponseEntity<Object> approve(long ownerId, Long bookingId, boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId + "?approved={approved}", ownerId, parameters, null);
    }

    public ResponseEntity<Object> getById(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getAllByBooker(long bookerId, BookingState state) {
        Map<String, Object> parameters = Map.of("state", state.name());
        return get("?state={state}", bookerId, parameters);
    }

    public ResponseEntity<Object> getAllByOwner(long ownerId, BookingState state) {
        Map<String, Object> parameters = Map.of("state", state.name());
        return get("/owner?state={state}", ownerId, parameters);
    }
}