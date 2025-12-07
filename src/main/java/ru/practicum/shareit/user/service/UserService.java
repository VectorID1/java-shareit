package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

public interface UserService {
    UserResponseDto create(UserCreateDto userCreateDto);

    UserResponseDto update(Long userId, UserUpdateDto userUpdateDto);

    UserResponseDto getById(Long userId);

    List<UserResponseDto> getAll();

    void delete(Long userId);
}
