package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    UserRepository userRepository;

    @Test
    void createUserAndSaveToDateBase() {
        UserDto userDto = UserDto.builder()
                .name("Alex")
                .email("Alex@mail.ru")
                .build();

        UserResponseDto userResponseDto = userService.create(userDto);

        assertNotNull(userResponseDto);

        Optional<User> user = userRepository.findById(userResponseDto.getId());
        assertTrue(user.isPresent());
        assertEquals("Alex", user.get().getName());
    }

    @Test
    void updateUserShouldUpdateInDataBase() {
        UserDto creteUser = UserDto.builder()
                .name("Alex")
                .email("Alex@mail.ru")
                .build();

        UserResponseDto userResponseDto = userService.create(creteUser);

        UserUpdateDto updateUser = UserUpdateDto.builder()
                .name("Update")
                .email("Update@mail.ru")
                .build();

        UserResponseDto update = userService.update(userResponseDto.getId(), updateUser);

        assertEquals("Update", update.getName());

        Optional<User> userFromDB = userRepository.findById(userResponseDto.getId());
        assertTrue(userFromDB.isPresent());
        assertEquals("Update", userFromDB.get().getName());
    }

    @Test
    void deleteUserIntegrationTest() {
        UserResponseDto user1 = userService.create(
                UserDto.builder()
                        .name("Alex")
                        .email("alex@test.com")
                        .build()
        );
        UserResponseDto user2 = userService.create(
                UserDto.builder()
                        .name("Olga")
                        .email("olga@test.com")
                        .build()
        );

        assertEquals(2, userRepository.count());

        userService.delete(user1.getId());

        assertEquals(1, userRepository.count());
        assertFalse(userRepository.existsById(user1.getId()));
        assertTrue(userRepository.existsById(user2.getId()));
    }
}
