package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {

    Long id;

    @NotBlank(message = "Описание не может быть пустым")
    String description;

    Long requesterId;

    LocalDateTime created;

    private List<ItemForRequestDto> items;
}
