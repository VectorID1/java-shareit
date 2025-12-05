package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

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

    public static Item toItem(ItemCreateDto itemCreateDto) {
        return Item.builder()
                .name(itemCreateDto.getName())
                .description(itemCreateDto.getDescription())
                .available(itemCreateDto.getAvailable())
                .build();
    }

    public static Item toItem(ItemUpdateDto updateDto) {
        return Item.builder()
                .name(updateDto.getName())
                .description(updateDto.getDescription())
                .available(updateDto.getAvailable())
                .build();
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
}
