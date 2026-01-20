package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    void createUser_whenValid_thenReturnCreate() throws Exception {

        UserDto inputUser = UserDto.builder()
                .name("Alex")
                .email("Alex@mail.ru")
                .build();

        UserResponseDto outputUser = UserResponseDto.builder()
                .id(1L)
                .name("Alex")
                .email("Alex@mail.ru")
                .build();
        when(userService.create(any(UserDto.class))).thenReturn(outputUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Alex"))
                .andExpect(jsonPath("$.email").value("Alex@mail.ru"));
    }

    @Test
    void createUser_WhenEmailAlreadyExist_thenReturnConflict() throws Exception {
        UserDto inputUser = UserDto.builder()
                .name("Alex")
                .email("Alex@mail.ru")
                .build();
        when(userService.create(any(UserDto.class))).thenThrow(new ConflictException("Пользователь с эл. почтой Alex@mail.ru уже существует"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUser)))
                .andExpect(status().isConflict());
    }

    @Test
    void getUserById_whenUserExist_thenReturnUser() throws Exception {
        UserResponseDto inputUser = UserResponseDto.builder()
                .id(1L)
                .name("Alex")
                .email("Alex@mail.ru")
                .build();
        when(userService.getById(1L)).thenReturn(inputUser);

        mockMvc.perform(get("/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alex"))
                .andExpect(jsonPath("$.email").value("Alex@mail.ru"));
    }

    @Test
    void getUserBiId_whenUserNotExist_thenReturnNotFound() throws Exception {
        when(userService.getById(999L)).thenThrow(new NotFoundException("Пользователя с id 999 не найден"));

        mockMvc.perform(get("/users/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserById_whenInvalidIdFormat_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/users/{id}", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsers_whenUsersExist() throws Exception {
        List<UserResponseDto> users = List.of(UserResponseDto.builder().id(1L)
                .name("Alex")
                .email("Alex@mail.ru")
                        .build(),
                UserResponseDto.builder()
                        .id(2L)
                        .name("Olga")
                        .email("Olga@mail.ru")
                        .build());

        when(userService.getAll(0, 10)).thenReturn(users);

        mockMvc.perform(get("/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alex"))
                .andExpect(jsonPath("$[0].email").value("Alex@mail.ru"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Olga"))
                .andExpect(jsonPath("$[1].email").value("Olga@mail.ru"));
    }

    @Test
    void updateUser_whenValid_thenReturnUpdated() throws Exception {
        UserDto updateUser = UserDto.builder()
                .name("Alex")
                .email("Alex@mail.ru")
                .build();

        UserResponseDto responseUser = UserResponseDto.builder()
                .id(1L)
                .name("Alex")
                .email("Alex@mail.ru")
                .build();
        when(userService.update(eq(1L), any(UserUpdateDto.class))).thenReturn(responseUser);

        mockMvc.perform(patch("/users/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alex"))
                .andExpect(jsonPath("$.email").value("Alex@mail.ru"));
    }

    @Test
    void deleteUserById_thenReturnOk() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/users/{id}", 1))
                .andExpect(status().isOk());

        verify(userService).delete(1L);
    }
}