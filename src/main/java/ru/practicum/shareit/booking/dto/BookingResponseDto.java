package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.constants.DateTimeFormats;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {

    private Long id;

    @JsonFormat(pattern = DateTimeFormats.ISO_8601)
    private LocalDateTime start;

    @JsonFormat(pattern = DateTimeFormats.ISO_8601)
    private LocalDateTime end;

    private BookingStatus status;
    private ItemResponseDto item;
    private UserResponseDto booker;

    @JsonFormat(pattern = DateTimeFormats.ISO_8601)
    private LocalDateTime created;
}