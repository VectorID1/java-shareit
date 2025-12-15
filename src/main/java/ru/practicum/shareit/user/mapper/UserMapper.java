package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.dto.UserResponseDto;

public class UserMapper {

    public static User toUser(UserCreateDto createDto) {
        return User.builder()
                .name(createDto.getName())
                .email(createDto.getEmail())
                .build();
    }

    public static User toUser(UserUpdateDto updateDto) {
        return User.builder()
                .name(updateDto.getName())
                .email(updateDto.getEmail())
                .build();
    }

    public static UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
