package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoJsonTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializedUserDto() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("Alex")
                .email("Alex@mail.ru")
                .build();
        String json = objectMapper.writeValueAsString(userDto);

        assertThat(json).contains("\"name\":\"Alex\"");
        assertThat(json).contains("\"email\":\"Alex@mail.ru\"");
    }

    @Test
    void deserializeUserDto() throws Exception {
        String json = "{\"name\":\"Alex\",\"email\":\"Alex@mail.ru\"}";

        UserDto userDto = objectMapper.readValue(json, UserDto.class);

        assertThat(userDto.getName()).isEqualTo("Alex");
        assertThat(userDto.getEmail()).isEqualTo("Alex@mail.ru");
    }

    @Test
    void serializeUserResponseDto() throws Exception {
        UserResponseDto userResponseDto = UserResponseDto.builder()
                .id(1L)
                .name("Alex")
                .email("Alex@mail.ru")
                .build();

        String json = objectMapper.writeValueAsString(userResponseDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Alex\"");
        assertThat(json).contains("\"email\":\"Alex@mail.ru\"");
    }
}
