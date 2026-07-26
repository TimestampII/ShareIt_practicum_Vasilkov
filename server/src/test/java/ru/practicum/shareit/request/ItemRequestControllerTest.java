package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Тестируем только слой контроллера: сервис замокан, проверяем маршрутизацию,
// сериализацию JSON и передачу заголовка X-Sharer-User-Id.
@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void create_shouldReturnCreatedRequestDto() throws Exception {
        NewItemRequestDto newRequestDto = new NewItemRequestDto("Нужна дрель");
        ItemRequestDto responseDto = new ItemRequestDto(1L, "Нужна дрель", LocalDateTime.now(), List.of());

        when(itemRequestService.create(1L, newRequestDto)).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getOwn_shouldReturnListFromService() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(1L, "Нужна дрель", LocalDateTime.now(), List.of());
        when(itemRequestService.getOwn(1L)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAll_shouldReturnListFromService() throws Exception {
        when(itemRequestService.getAll(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/requests/all").header(USER_ID_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(itemRequestService).getAll(2L);
    }

    @Test
    void getById_shouldReturnRequestDto() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(5L, "Нужна дрель", LocalDateTime.now(), List.of());
        when(itemRequestService.getById(1L, 5L)).thenReturn(requestDto);

        mockMvc.perform(get("/requests/5").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }
}