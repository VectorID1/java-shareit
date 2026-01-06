package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public UserResponseDto getUser(@PathVariable("userId") Long userId) {
        log.info("GET /users/{} - получение пользователя", userId);
        return userService.getById(userId);
    }

    @GetMapping
    public List<UserResponseDto> getAll() {
        log.info("GET /users - получение всех пользователей");
        return userService.getAll();
    }

    @PostMapping
    public UserResponseDto create(@Valid @RequestBody UserCreateDto userCreateDto) {
        log.info("POST /users - создание пользователя: name={}, email={}",
                userCreateDto.getName(), userCreateDto.getEmail());
        return userService.create(userCreateDto);
    }

    @PatchMapping("/{userId}")
    public UserResponseDto update(@PathVariable Long userId,
                                  @Valid @RequestBody UserUpdateDto userUpdateDto) {
        log.info("PATCH /users/{} - обновление пользователя", userId);
        return userService.update(userId, userUpdateDto);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        log.info("DELETE /users/{} - удаление пользователя", userId);
        userService.delete(userId);
    }
}
