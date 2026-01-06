package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constants.HttpHeadersConstants;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemResponseDto create(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long ownerId,
            @Valid @RequestBody ItemCreateDto itemCreateDto) {
        log.info("POST /items - создание вещи '{}' владельцем {}",
                itemCreateDto.getName(), ownerId);
        return itemService.create(itemCreateDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long ownerId,
            @PathVariable Long itemId,
            @RequestBody ItemUpdateDto updateDto) {
        log.info("PATCH /items/{} - обновление владельцем {}", itemId, ownerId);
        return itemService.update(itemId, updateDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getItemById(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long userId,
            @PathVariable Long itemId) {
        log.info("GET /items/{} - получение пользователем {}", itemId, userId);
        return itemService.getByItemId(itemId, userId);
    }

    @GetMapping
    public List<ItemResponseDto> getAllItemsByOwner(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long ownerId) {
        log.info("GET /items - все вещи владельца {}", ownerId);
        return itemService.getAllItemsByOwnerId(ownerId);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> search(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long userId,
            @RequestParam String text) {
        log.info("GET /items/search?text={} - поиск пользователем {}", text, userId);
        return itemService.search(text, userId);
    }

    @DeleteMapping("/{itemId}")
    public void delete(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long ownerId,
            @PathVariable Long itemId) {
        log.info("DELETE /items/{} - удаление владельцем {}", itemId, ownerId);
        itemService.delete(itemId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentCreateDto commentDto) {
        log.info("POST /items/{}/comment - добавление комментария пользователем {}",
                itemId, userId);
        return itemService.addComment(itemId, commentDto, userId);
    }
}