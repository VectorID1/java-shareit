package ru.practicum.shareit.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public UserResponseDto getUser(@PathVariable("userId") Long userId) {
        return UserMapper.toResponseDto(userService.getById(userId));
    }

    @GetMapping
    public List<UserResponseDto> getAll() {
        return userService.getAll().stream()
                .map(UserMapper::toResponseDto)
                .toList();
    }

    @PostMapping
    public UserResponseDto create(@Valid @RequestBody UserCreateDto userCreateDto) {
        User user = UserMapper.toUser(userCreateDto);
        User savedUser = userService.create(user);

        return UserMapper.toResponseDto(savedUser);
    }

    @PatchMapping("/{userId}")
    public UserResponseDto update(@PathVariable Long userId,
                                  @Valid @RequestBody UserUpdateDto userUpdateDto) {
        User user = UserMapper.toUser(userUpdateDto);
        User updatedUser = userService.update(userId, user);
        return UserMapper.toResponseDto(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}
