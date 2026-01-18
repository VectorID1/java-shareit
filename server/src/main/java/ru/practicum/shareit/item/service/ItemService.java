package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {

    ItemResponseDto create(ItemDto itemDto, Long ownerId);

    ItemResponseDto update(Long itemId, ItemUpdateDto itemUpdateDto, Long ownerId);

    ItemResponseDto getById(Long itemId);

    List<ItemResponseDto> search(String text, Long userId, Integer from, Integer size);

    void delete(Long itemId, Long userId);

    CommentDto addComment(Long itemId, CommentCreateDto createDto, Long userId);

    ItemResponseDto getByItemId(Long itemId, Long userId);

    List<ItemResponseDto> getAllItemsByOwnerId(Long ownerId, Integer from, Integer size);
}
