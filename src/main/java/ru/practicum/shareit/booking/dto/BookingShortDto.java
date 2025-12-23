package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.constants.DateTimeFormats;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingShortDto {
    private Long id;
    private Long bookerId;

    @JsonFormat(pattern = DateTimeFormats.ISO_8601)
    private LocalDateTime start;

    @JsonFormat(pattern = DateTimeFormats.ISO_8601)
    private LocalDateTime end;

}