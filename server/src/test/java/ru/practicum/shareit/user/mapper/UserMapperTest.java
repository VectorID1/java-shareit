package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void instantiateMapper() {
        UserMapper mapper = new UserMapper();
        assertNotNull(mapper);
    }

    @Test
    void toUser_whenValidDto_thenSuccess() {

        UserDto dto = UserDto.builder()
                .name("Alex")
                .email("Alex@email.com")
                .build();

        User user = UserMapper.toUser(dto);

        assertNotNull(user);
        assertEquals("Alex", user.getName());
        assertEquals("Alex@email.com", user.getEmail());
        assertNull(user.getId());
    }

    @Test
    void toResponseDto_whenValidUser_thenSuccess() {
        User user = User.builder()
                .id(1L)
                .name("Alex")
                .email("Alex@email.com")
                .build();

        UserResponseDto responseDto = UserMapper.toResponseDto(user);

        assertNotNull(responseDto);
        assertEquals(1L, responseDto.getId());
        assertEquals("Alex", responseDto.getName());
        assertEquals("Alex@email.com", responseDto.getEmail());
    }

}