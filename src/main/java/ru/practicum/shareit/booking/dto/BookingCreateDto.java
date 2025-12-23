package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.constants.DateTimeFormats;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateDto {

    @NotNull(message = "Id вещи не может быть пустым")
    private Long itemId;

    @NotNull(message = "Дата начала бронирования не может быть пустой")
    @FutureOrPresent(message = "Дата начала должна быть в будущем или настоящем")
    @JsonFormat(pattern = DateTimeFormats.ISO_8601)
    private LocalDateTime start;

    @NotNull(message = "Дата окончания бронирования не может быть пустой")
    @Future(message = "Дата окончания должна быть в будущем")
    @JsonFormat(pattern = DateTimeFormats.ISO_8601)
    private LocalDateTime end;
}
