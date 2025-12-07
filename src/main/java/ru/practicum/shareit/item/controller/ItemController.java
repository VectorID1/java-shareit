package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constants.HttpHeadersConstants;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemResponseDto create(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long ownerId,
            @Valid @RequestBody ItemCreateDto itemCreateDto) {

        return itemService.create(itemCreateDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long ownerId,
            @PathVariable Long itemId,
            @RequestBody ItemUpdateDto updateDto) {

        return itemService.update(itemId, updateDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getById(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long userId,
            @PathVariable Long itemId) {

        return itemService.getById(itemId);
    }

    @GetMapping
    public List<ItemResponseDto> getAllByOwner(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long ownerId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        return itemService.getAllByOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> search(
            @RequestParam String text) {

        return itemService.search(text);
    }

    @DeleteMapping("/{itemId}")
    public void delete(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long ownerId,
            @PathVariable Long itemId) {

        itemService.delete(itemId);
    }
}
