package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void create_shouldSaveUserAndReturnDtoWithId() {
        UserDto newUserDto = new UserDto(null, "Иван", "ivan@mail.com");

        UserDto result = userService.create(newUserDto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Иван");
        assertThat(result.getEmail()).isEqualTo("ivan@mail.com");
        assertThat(userRepository.findById(result.getId())).isPresent();
    }

    @Test
    void create_shouldThrowDuplicateEmailException_whenEmailAlreadyExists() {
        userService.create(new UserDto(null, "Иван", "same@mail.com"));
        UserDto second = new UserDto(null, "Пётр", "same@mail.com");

        assertThrows(DuplicateEmailException.class, () -> userService.create(second));
    }

    @Test
    void update_shouldChangeOnlyProvidedFields() {
        UserDto created = userService.create(new UserDto(null, "Иван", "ivan@mail.com"));

        UserDto patch = new UserDto(null, "Новое имя", null);
        UserDto updated = userService.update(created.getId(), patch);

        assertThat(updated.getName()).isEqualTo("Новое имя");
        assertThat(updated.getEmail()).isEqualTo("ivan@mail.com");
    }

    @Test
    void update_shouldChangeEmail_whenProvided() {
        UserDto created = userService.create(new UserDto(null, "Иван", "ivan@mail.com"));

        UserDto patch = new UserDto(null, null, "new@mail.com");
        UserDto updated = userService.update(created.getId(), patch);

        assertThat(updated.getEmail()).isEqualTo("new@mail.com");
        assertThat(updated.getName()).isEqualTo("Иван");
    }

    @Test
    void update_shouldThrowDuplicateEmailException_whenNewEmailBelongsToAnotherUser() {
        UserDto first = userService.create(new UserDto(null, "Иван", "ivan@mail.com"));
        userService.create(new UserDto(null, "Пётр", "petr@mail.com"));

        UserDto patch = new UserDto(null, null, "petr@mail.com");

        assertThrows(DuplicateEmailException.class, () -> userService.update(first.getId(), patch));
    }

    @Test
    void update_shouldThrowNotFoundException_whenUserNotExists() {
        long unknownId = 999_999L;
        UserDto patch = new UserDto(null, "Имя", null);

        assertThrows(NotFoundException.class, () -> userService.update(unknownId, patch));
    }

    @Test
    void getById_shouldReturnUser() {
        UserDto created = userService.create(new UserDto(null, "Иван", "ivan@mail.com"));

        UserDto result = userService.getById(created.getId());

        assertThat(result.getEmail()).isEqualTo("ivan@mail.com");
    }

    @Test
    void getById_shouldThrowNotFoundException_whenUserNotExists() {
        long unknownId = 999_999L;

        assertThrows(NotFoundException.class, () -> userService.getById(unknownId));
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        userService.create(new UserDto(null, "Иван", "ivan@mail.com"));
        userService.create(new UserDto(null, "Пётр", "petr@mail.com"));

        List<UserDto> all = userService.getAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void delete_shouldRemoveUser() {
        UserDto created = userService.create(new UserDto(null, "Иван", "ivan@mail.com"));

        userService.delete(created.getId());

        assertThat(userRepository.findById(created.getId())).isEmpty();
    }

    @Test
    void delete_shouldThrowNotFoundException_whenUserNotExists() {
        long unknownId = 999_999L;

        assertThrows(NotFoundException.class, () -> userService.delete(unknownId));
    }
}