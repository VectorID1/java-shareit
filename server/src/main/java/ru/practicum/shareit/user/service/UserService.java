package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

public interface UserService {

    UserResponseDto create(UserDto userDto);

    UserResponseDto update(Long userId, UserUpdateDto userUpdateDto);

    UserResponseDto getById(Long userId);

    List<UserResponseDto> getAll(Integer from, Integer size);

    void delete(Long userId);
}
