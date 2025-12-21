package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponseDto create(UserCreateDto userCreateDto) {
        validateEmailUniqueness(userCreateDto.getEmail());
        User user = UserMapper.toUser(userCreateDto);

        return UserMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto update(Long userId, UserUpdateDto userUpdates) {
        User existingUser = getUserById(userId);

        if (userUpdates.getEmail() != null &&
                !userUpdates.getEmail().equals(existingUser.getEmail())) {

            validateEmailUniqueness(userUpdates.getEmail());
            existingUser.setEmail(userUpdates.getEmail());
        }

        if (userUpdates.getName() != null) {
            existingUser.setName(userUpdates.getName());
        }

        return UserMapper.toResponseDto(existingUser);
    }

    @Override
    public UserResponseDto getById(Long userId) {
        User user = getUserById(userId);

        return UserMapper.toResponseDto(user);
    }

    @Override
    public List<UserResponseDto> getAll() {

        return userRepository.findAll().stream()
                .map(UserMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        User user = getUserById(userId);

        userRepository.delete(user);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(
                    "Пользователь с эл. почтой " + email + " уже существует");
        }
    }
}
