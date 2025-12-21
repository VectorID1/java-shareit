package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    ItemResponseDto create(ItemCreateDto itemCreateDto, Long ownerId);

    ItemResponseDto update(Long itemId, ItemUpdateDto itemUpdateDto, Long ownerId);

    ItemResponseDto getById(Long itemId);

    List<ItemResponseDto> getAllByOwner(Long ownerId);

    List<ItemResponseDto> search(String text, Long userId);

    void delete(Long itemId);

    CommentDto addComment(Long itemId, CommentCreateDto createDto, Long userId);

    ItemResponseDto getByItemId(Long itemId, Long userId);

    List<ItemResponseDto> getAllItemsByOwnerId(Long ownerId);
}
