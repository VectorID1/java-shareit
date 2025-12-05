package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemResponseDto create(
            @RequestHeader(USER_ID_HEADER) Long ownerId,
            @Valid @RequestBody ItemCreateDto itemCreateDto) {

        Item item = ItemMapper.toItem(itemCreateDto);
        Item saveItem = itemService.create(item, ownerId);
        return ItemMapper.toResponseDto(saveItem);
    }

    @PatchMapping("/{itemId}")
    public ItemResponseDto update(
            @RequestHeader(USER_ID_HEADER) Long ownerId,
            @PathVariable Long itemId,
            @RequestBody ItemUpdateDto updateDto) {

        Item itemUpdates = ItemMapper.toItem(updateDto);

        Item updatedItem = itemService.update(itemId, itemUpdates, ownerId);

        return ItemMapper.toResponseDto(updatedItem);
    }

    @GetMapping("/{itemId}")
    public ItemResponseDto getById(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long itemId) {
        Item item = itemService.getById(itemId);
        return ItemMapper.toResponseDto(item);
    }

    @GetMapping
    public List<ItemResponseDto> getAllByOwner(
            @RequestHeader(USER_ID_HEADER) Long ownerId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        List<Item> items = itemService.getAllByOwner(ownerId);

        return items.stream()
                .skip(from)
                .limit(size)
                .map(ItemMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemResponseDto> search(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        List<Item> items = itemService.search(text);

        return items.stream()
                .skip(from)
                .limit(size)
                .map(ItemMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{itemId}")
    public void delete(
            @RequestHeader(USER_ID_HEADER) Long ownerId,
            @PathVariable Long itemId) {

        itemService.delete(itemId);
    }
}
