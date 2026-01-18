package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponseDto {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private Long requestId;

    private BookingShortDto lastBooking = null;

    private BookingShortDto nextBooking = null;

    @Builder.Default
    private List<CommentDto> comments = new ArrayList<>();
}
