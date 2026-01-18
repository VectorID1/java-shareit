package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(ItemRequestDto requestDto, Long userId);

    List<ItemRequestDto> getOwnerRequest(Long userId);

    ItemRequestDto getRequestById(Long requestId, Long userId);

    List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size);
}
