package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

public interface ItemService {
    ItemResponseDto create(ItemCreateDto itemCreateDto, Long ownerId);

    ItemResponseDto update(Long itemId, ItemUpdateDto itemUpdateDto, Long ownerId);

    ItemResponseDto getById(Long itemId);

    List<ItemResponseDto> getAllByOwner(Long ownerId);

    List<ItemResponseDto> search(String text);

    void delete(Long itemId);
}
