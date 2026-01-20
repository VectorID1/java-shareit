package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ItemMapper {

    public static ItemResponseDto toResponseDto(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static Item toItem(ItemDto itemDto) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public static ItemResponseDto toResponseDto(Item item,
                                                BookingShortDto lastBooking,
                                                BookingShortDto nextBooking, List<CommentDto> comments) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments)
                .build();
    }

    public ItemForRequestDto toItemForRequestDto(Item item) {
        if (item == null) {
            return null;
        }

        return ItemForRequestDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(getRequestId(item))
                .ownerId(getOwnerId(item))
                .build();
    }

    public List<ItemForRequestDto> toItemForRequestDtoList(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        return items.stream()
                .map(this::toItemForRequestDto)
                .collect(Collectors.toList());
    }

    private Long getRequestId(Item item) {
        return item.getRequest() != null ? item.getRequest().getId() : null;
    }

    private Long getOwnerId(Item item) {
        return item.getOwner() != null ? item.getOwner().getId() : null;
    }


    public static void updateItemFromDto(ItemUpdateDto updateDto, Item existingItem) {
        if (updateDto.getName() != null) {
            existingItem.setName(updateDto.getName());
        }
        if (updateDto.getDescription() != null) {
            existingItem.setDescription(updateDto.getDescription());
        }
        if (updateDto.getAvailable() != null) {
            existingItem.setAvailable(updateDto.getAvailable());
        }
    }

    public static void updateItemRequest(ItemUpdateDto dto, Item item, ItemRequest request) {
        if (dto.getRequestId() != null) {
            if (dto.getRequestId() <= 0) {
                item.setRequest(null);
            } else if (request != null) {
                item.setRequest(request);
            }
        }
    }
}
