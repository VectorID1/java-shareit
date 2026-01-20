package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.constants.DateTimeFormats;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {

	@NotNull(message = "Id вещи не может быть пустым")
	private long itemId;

	@NotNull(message = "Дата начала бронирования не может быть пустой")
	@FutureOrPresent(message = "Дата начала должна быть в будущем или настоящем")
	@JsonFormat(pattern = DateTimeFormats.DATE_TIME_FORMAT)
	private LocalDateTime start;

	@NotNull(message = "Дата окончания бронирования не может быть пустой")
	@Future(message = "Дата окончания должна быть в будущем")
	@JsonFormat(pattern = DateTimeFormats.DATE_TIME_FORMAT)
	private LocalDateTime end;
}
