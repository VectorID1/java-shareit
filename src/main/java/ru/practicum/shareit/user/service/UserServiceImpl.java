package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User create(User user) {
        if (isEmailExists(user.getEmail())) {
            throw new ConflictException("Пользователь с эл. почтой " + user.getEmail() + " уже существует");
        }
        return userRepository.save(user);
    }

    @Override
    public User update(Long userId, User userUpdates) {
        User existingUser = getById(userId);

        if (userUpdates.getEmail() != null &&
                !userUpdates.getEmail().equals(existingUser.getEmail())) {

            if (isEmailExists(userUpdates.getEmail())) {
                throw new ConflictException("Email " + userUpdates.getEmail() + " уже используется");
            }
            existingUser.setEmail(userUpdates.getEmail());
        }

        if (userUpdates.getName() != null) {
            existingUser.setName(userUpdates.getName());
        }

        return userRepository.update(existingUser);
    }

    @Override
    public User getById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public void delete(Long userId) {
        if (!userRepository.findById(userId).isPresent()) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        userRepository.delete(userId);
    }

    private boolean isEmailExists(String email) {
        return userRepository.findAll().stream()
                .anyMatch(user -> email.equals(user.getEmail()));
    }
}
