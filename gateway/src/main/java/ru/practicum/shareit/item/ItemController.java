package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;


@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @RequestBody @Valid ItemRequestDto requestDto) {
        log.info("Creating item {}, userId={}", requestDto, userId);
        return itemClient.createItem(userId, requestDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @PathVariable Long itemId,
            @RequestBody ItemRequestDto requestDto) {
        log.info("PATCH /items/{} - обновление владельцем {}", itemId, ownerId);
        return itemClient.updateItem(ownerId, itemId, requestDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity getItemById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId) {
        log.info("GET /items/{} - получение пользователем {}", itemId, userId);
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /items - все вещи владельца {}", ownerId);
        return itemClient.getUserItems(ownerId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam String text,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("GET /items/search?text={} - поиск пользователем {}", text, userId);
        return itemClient.searchItems(userId, text, from, size);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> delete(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @PathVariable Long itemId) {
        log.info("DELETE /items/{} - удаление владельцем {}", itemId, ownerId);
        return itemClient.deleteItem(itemId, ownerId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequestDto requestDto) {
        log.info("POST /items/{}/comment - добавление комментария пользователем {}",
                itemId, userId);
        return itemClient.addComment(userId, itemId, requestDto);
    }
}
