package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestMapperTest {

    private final ItemRequestMapper mapper = new ItemRequestMapper(new ItemMapper());

    @Test
    void testItemRequestDto() {
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Test")
                .requester(User.builder()
                        .id(1L)
                        .build())
                .build();
        assertNotNull(mapper.itemRequestDto(request));
    }

    @Test void testToRequestDtoWithoutItems() {
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Test")
                .requester(User.builder()
                        .id(1L)
                        .build())
                .build();
        assertNotNull(mapper.toRequestDtoWithoutItems(request));
    }

    @Test void testToRequestDtoList() {
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Test")
                .build();
        assertEquals(1, mapper.toRequestDtoList(List.of(request)).size());
    }
}