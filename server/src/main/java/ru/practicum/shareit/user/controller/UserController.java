package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
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
    public List<UserResponseDto> getAll(
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /users - получение всех пользователей");
        return userService.getAll(from, size);
    }

    @PostMapping
    public UserResponseDto create(@RequestBody UserDto userDto) {
        log.info("POST /users - создание пользователя: name={}, email={}",
                userDto.getName(), userDto.getEmail());
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserResponseDto update(@PathVariable Long userId,
                                  @RequestBody UserUpdateDto userUpdateDto) {
        log.info("PATCH /users/{} - обновление пользователя", userId);
        return userService.update(userId, userUpdateDto);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        log.info("DELETE /users/{} - удаление пользователя", userId);
        userService.delete(userId);
    }
}
