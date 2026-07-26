package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.ItemShortDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingService bookingService;

    @Test
    void create_shouldReturnCreatedBooking() throws Exception {
        NewBookingDto newBookingDto = new NewBookingDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        BookingResponseDto responseDto = new BookingResponseDto(
                1L, newBookingDto.getStart(), newBookingDto.getEnd(), BookingStatus.WAITING,
                new ItemShortDto(1L, "Дрель"), new UserShortDto(2L));
        when(bookingService.create(2L, newBookingDto)).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 2L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newBookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approve_shouldReturnApprovedBooking() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(
                1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1), BookingStatus.APPROVED,
                new ItemShortDto(1L, "Дрель"), new UserShortDto(2L));
        when(bookingService.approve(1L, 1L, true)).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getById_shouldReturnBooking() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(
                1L, LocalDateTime.now(), LocalDateTime.now().plusDays(1), BookingStatus.WAITING,
                new ItemShortDto(1L, "Дрель"), new UserShortDto(2L));
        when(bookingService.getById(2L, 1L)).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/1").header(USER_ID_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.name").value("Дрель"));
    }

    @Test
    void getAllByBooker_shouldDefaultToAllState() throws Exception {
        when(bookingService.getAllByBooker(2L, BookingState.ALL)).thenReturn(List.of());

        mockMvc.perform(get("/bookings").header(USER_ID_HEADER, 2L))
                .andExpect(status().isOk());

        verify(bookingService).getAllByBooker(2L, BookingState.ALL);
    }

    @Test
    void getAllByBooker_shouldParseExplicitState() throws Exception {
        when(bookingService.getAllByBooker(eq(2L), eq(BookingState.WAITING))).thenReturn(List.of());

        mockMvc.perform(get("/bookings").header(USER_ID_HEADER, 2L).param("state", "WAITING"))
                .andExpect(status().isOk());

        verify(bookingService).getAllByBooker(2L, BookingState.WAITING);
    }

    @Test
    void getAllByBooker_shouldReturnBadRequest_whenStateUnknown() throws Exception {
        mockMvc.perform(get("/bookings").header(USER_ID_HEADER, 2L).param("state", "NONSENSE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllByOwner_shouldReturnListFromService() throws Exception {
        when(bookingService.getAllByOwner(1L, BookingState.ALL)).thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingService).getAllByOwner(1L, BookingState.ALL);
    }
}