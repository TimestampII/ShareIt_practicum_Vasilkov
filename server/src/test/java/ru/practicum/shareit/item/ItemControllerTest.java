package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.dto.NewCommentDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemService itemService;

    @Test
    void create_shouldReturnCreatedItem() throws Exception {
        ItemDto requestDto = new ItemDto(null, "Дрель", "Описание", true, null);
        ItemDto responseDto = new ItemDto(1L, "Дрель", "Описание", true, null);
        when(itemService.create(1L, requestDto)).thenReturn(responseDto);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void update_shouldReturnUpdatedItem() throws Exception {
        ItemDto patchDto = new ItemDto(null, "Новое имя", null, null, null);
        ItemDto responseDto = new ItemDto(1L, "Новое имя", "Описание", true, null);
        when(itemService.update(1L, 1L, patchDto)).thenReturn(responseDto);

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое имя"));
    }

    @Test
    void getById_shouldReturnItemWithBookings() throws Exception {
        ItemWithBookingsDto responseDto = new ItemWithBookingsDto(
                1L, "Дрель", "Описание", true, null, null, List.of());
        when(itemService.getById(1L, 1L)).thenReturn(responseDto);

        mockMvc.perform(get("/items/1").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.comments").isArray());
    }

    @Test
    void getAllByOwner_shouldReturnListFromService() throws Exception {
        ItemWithBookingsDto item = new ItemWithBookingsDto(
                1L, "Дрель", "Описание", true, null, null, List.of());
        when(itemService.getAllByOwner(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/items").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void search_shouldReturnMatchingItems() throws Exception {
        ItemDto found = new ItemDto(1L, "Дрель Bosch", "Описание", true, null);
        when(itemService.search("дрель")).thenReturn(List.of(found));

        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель Bosch"));
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        NewCommentDto newCommentDto = new NewCommentDto("Отличная вещь!");
        CommentDto responseDto = new CommentDto(1L, "Отличная вещь!", "Автор", LocalDateTime.now());
        when(itemService.addComment(1L, 1L, newCommentDto)).thenReturn(responseDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(newCommentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Отличная вещь!"));
    }
}