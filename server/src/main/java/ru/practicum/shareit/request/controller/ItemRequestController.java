package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constants.HttpHeadersConstants;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createRequest(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long userId,
            @RequestBody ItemRequestDto itemRequestDto) {
        log.info("POST /requests - создание запроса пользователем {}", userId);
        return itemRequestService.create(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getOwnerRequests(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long userId) {
        log.info("GET /requests - получение запросов пользователя {}", userId);
        return itemRequestService.getOwnerRequest(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /requests/all - получение всех запросов пользователем {} , from={}, size={}",
                userId, from, size);
        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long userId,
            @PathVariable Long requestId) {
        log.info("GET /requests/{} - получение  запроса пользователем {}", requestId, userId);
        return itemRequestService.getRequestById(requestId, userId);
    }
}
