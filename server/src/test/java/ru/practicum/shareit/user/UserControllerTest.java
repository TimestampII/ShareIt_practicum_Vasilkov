package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    @Test
    void create_shouldReturnCreatedUser() throws Exception {
        UserDto requestDto = new UserDto(null, "Иван", "ivan@mail.com");
        UserDto responseDto = new UserDto(1L, "Иван", "ivan@mail.com");
        when(userService.create(requestDto)).thenReturn(responseDto);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Иван"))
                .andExpect(jsonPath("$.email").value("ivan@mail.com"));
    }

    @Test
    void update_shouldReturnUpdatedUser() throws Exception {
        UserDto patchDto = new UserDto(null, "Новое имя", null);
        UserDto responseDto = new UserDto(1L, "Новое имя", "ivan@mail.com");
        when(userService.update(1L, patchDto)).thenReturn(responseDto);

        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое имя"))
                .andExpect(jsonPath("$.email").value("ivan@mail.com"));
    }

    @Test
    void getById_shouldReturnUser() throws Exception {
        UserDto responseDto = new UserDto(1L, "Иван", "ivan@mail.com");
        when(userService.getById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ivan@mail.com"));
    }

    @Test
    void getAll_shouldReturnListFromService() throws Exception {
        UserDto first = new UserDto(1L, "Иван", "ivan@mail.com");
        UserDto second = new UserDto(2L, "Пётр", "petr@mail.com");
        when(userService.getAll()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Иван"))
                .andExpect(jsonPath("$[1].name").value("Пётр"));
    }

    @Test
    void delete_shouldCallServiceAndReturnOk() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService).delete(1L);
    }
}