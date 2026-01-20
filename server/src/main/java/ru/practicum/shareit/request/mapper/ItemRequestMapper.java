package ru.practicum.shareit.request.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemRequestMapper {
    private final ItemMapper itemMapper;

    public ItemRequestDto itemRequestDto(ItemRequest request) {
        if (request == null) return null;

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requesterId(request.getRequester() != null ?
                        request.getRequester().getId() : null)
                .created(request.getCreated())
                .items(itemMapper.toItemForRequestDtoList(request.getItems()))
                .build();
    }

    public ItemRequestDto toRequestDtoWithoutItems(ItemRequest request) {
        if (request == null) {
            return null;
        }
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requesterId(request.getRequester().getId())
                .created(request.getCreated())
                .items(List.of())
                .build();
    }

    public List<ItemRequestDto> toRequestDtoList(List<ItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        return requests.stream()
                .map(this::itemRequestDto)
                .collect(Collectors.toList());
    }
}