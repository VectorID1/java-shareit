package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User defaultUser;

    @BeforeEach
    void setUp() {
        defaultUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@email.ru")
                .build();
    }

    @Test
    void create_whenValidData_thenSuccess() {
        UserDto inputDto = UserDto.builder()
                .name("New User")
                .email("new@email.ru")
                .build();
        User savedUser = User.builder()
                .id(1L)
                .name("New User")
                .email("new@email.ru")
                .build();

        when(userRepository.existsByEmail("new@email.ru")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto result = userService.create(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New User", result.getName());
        assertEquals("new@email.ru", result.getEmail());

        verify(userRepository).save(argThat(user ->
                user.getName().equals("New User") &&
                        user.getEmail().equals("new@email.ru")
        ));
    }

    @Test
    void create_whenEmailAlreadyExists_thenThrowConflictException() {
        when(userRepository.existsByEmail("existing@email.ru")).thenReturn(true);
        UserDto inputDto = UserDto.builder()
                .name("User")
                .email("existing@email.ru")
                .build();

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.create(inputDto)
        );

        assertEquals("Пользователь с эл. почтой existing@email.ru уже существует",
                exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_whenUserExists_thenSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(defaultUser));

        UserResponseDto result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@email.ru", result.getEmail());

        verify(userRepository).findById(1L);
    }

    @Test
    void getById_whenUserNotExists_thenThrowNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getById(999L)
        );

        assertEquals("Пользователь с id 999 не найден", exception.getMessage());
    }

    @Test
    void getAll_whenUsersExist_thenReturnList() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        User user1 = User.builder()
                .id(1L)
                .name("John")
                .email("john@email.ru")
                .build();
        User user2 = User.builder()
                .id(2L)
                .name("Jane")
                .email("jane@mail.ru")
                .build();
        Page<User> userPage = new PageImpl<>(List.of(user1, user2));

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        List<UserResponseDto> result = userService.getAll(0, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getName());
        assertEquals("Jane", result.get(1).getName());
    }

    @Test
    void getAll_whenNoUsers_thenReturnEmptyList() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        Page<User> emptyPage = Page.empty();

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        List<UserResponseDto> result = userService.getAll(0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void update_whenValidFullUpdate_thenSuccess() {
        Long userId = 1L;
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Updated Name")
                .email("updated@email.ru")
                .build();
        User existingUser = User.builder()
                .id(userId)
                .name("Updated Name")
                .email("old@email.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("updated@email.ru")).thenReturn(false);

        UserResponseDto result = userService.update(userId, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@email.ru", result.getEmail());

        assertEquals("Updated Name", existingUser.getName());
        assertEquals("updated@email.ru", existingUser.getEmail());
    }

    @Test
    void update_whenUpdateOnlyName_thenKeepEmail() {
        Long userId = 1L;
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Updated Name")
                .build();

        User existingUser = User.builder()
                .id(userId)
                .name("Old Name")
                .email("old@email.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        UserResponseDto result = userService.update(userId, updateDto);

        assertEquals("Updated Name", result.getName());
        assertEquals("old@email.ru", result.getEmail());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void update_whenUpdateOnlyEmail_thenKeepName() {
        Long userId = 1L;
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("updated@email.ru")
                .build();

        User existingUser = User.builder()
                .id(userId)
                .name("Old Name")
                .email("old@email.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("updated@email.ru")).thenReturn(false);

        UserResponseDto result = userService.update(userId, updateDto);

        assertEquals("Old Name", result.getName()); // Name остался старый
        assertEquals("updated@email.ru", result.getEmail());
    }

    @Test
    void update_whenEmailNotChanged_thenNoValidationNeeded() {
        Long userId = 1L;
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("same@email.ru")
                .build();

        User existingUser = User.builder()
                .id(userId)
                .name("Name")
                .email("same@email.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        UserResponseDto result = userService.update(userId, updateDto);

        assertEquals("same@email.ru", result.getEmail());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void update_whenNewEmailAlreadyExists_thenThrowConflictException() {
        Long userId = 1L;
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("existing@email.ru")
                .build();

        User existingUser = User.builder()
                .id(userId)
                .name("Name")
                .email("old@email.ru").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("existing@email.ru")).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> userService.update(userId, updateDto)
        );

        assertEquals("Пользователь с эл. почтой existing@email.ru уже существует",
                exception.getMessage());
    }

    @Test
    void update_whenUserNotFound_thenThrowNotFoundException() {
        Long userId = 999L;
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Updated")
                .email("updated@email.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.update(userId, updateDto)
        );

        assertEquals("Пользователь с id 999 не найден", exception.getMessage());
        verify(userRepository, never()).existsByEmail(anyString());
    }


    @Test
    void delete_whenUserExists_thenSuccess() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(defaultUser));

        userService.delete(userId);

        verify(userRepository).delete(defaultUser);
    }

    @Test
    void delete_whenUserNotExists_thenThrowNotFoundException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.delete(userId)
        );

        assertEquals("Пользователь с id 999 не найден", exception.getMessage());
        verify(userRepository, never()).delete(any());
    }


    @Test
    void update_whenUpdateDtoIsNull_thenNoChanges() {
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .name("Name")
                .email("email@test.ru")
                .build();
        UserUpdateDto updateUser = UserUpdateDto.builder()
                .name("Name")
                .email("email@test.ru")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        UserResponseDto result = userService.update(userId, updateUser);

        assertEquals("Name", result.getName());
        assertEquals("email@test.ru", result.getEmail());
    }
}