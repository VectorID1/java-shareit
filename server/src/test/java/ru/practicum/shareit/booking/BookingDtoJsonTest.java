package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeBookingDto() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.of(2025, 1, 25, 8, 0))
                .end(LocalDateTime.of(2025, 1, 30, 22, 0))
                .build();

        String json = objectMapper.writeValueAsString(bookingDto);

        assertThat(json).contains("\"itemId\":1");
        assertThat(json).contains("2025-01-25T08:00:00");
        assertThat(json).contains("2025-01-30T22:00:00");
    }

    @Test
    void deserializeBookingDto() throws Exception {
        String json = "{\"itemId\":1,\"start\":\"2025-01-25T08:00:00\",\"end\":\"2025-01-30T22:00:00\"}";

        BookingDto dto = objectMapper.readValue(json, BookingDto.class);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 1, 25, 8, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 1, 30, 22, 0));
    }
}
