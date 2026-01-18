package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    private final ItemMapper mapper = new ItemMapper();

    @Test
    void testToResponseDto() {
        Item item = Item.builder()
                .id(1L)
                .name("Test")
                .description("Desc")
                .available(true)
                .build();
        assertNotNull(ItemMapper.toResponseDto(item));
    }

    @Test
    void testToResponseDtoWithAll() {
        Item item = Item.builder()
                .id(1L)
                .name("Test")
                .build();
        BookingShortDto last = BookingShortDto.builder()
                .id(10L)
                .build();
        BookingShortDto next = BookingShortDto.builder()
                .id(11L)
                .build();
        CommentDto comment = CommentDto.builder()
                .id(20L)
                .text("Good")
                .build();
        assertNotNull(ItemMapper.toResponseDto(item, last, next, List.of(comment)));
    }

    @Test
    void testToItem() {
        ItemDto dto = ItemDto.builder()
                .name("Test")
                .description("Desc")
                .available(true)
                .build();
        assertNotNull(ItemMapper.toItem(dto));
    }

    @Test
    void testToItemForRequestDto() {
        Item item = Item.builder()
                .id(1L)
                .name("Test")
                .owner(User.builder()
                        .id(10L)
                        .build())
                .build();
        assertNotNull(mapper.toItemForRequestDto(item));
    }

    @Test
    void testToItemForRequestDtoList() {
        Item item = Item.builder()
                .id(1L)
                .name("Test")
                .build();
        assertEquals(1, mapper.toItemForRequestDtoList(List.of(item)).size());
    }

    @Test
    void testUpdateItemFromDto() {
        Item item = Item.builder()
                .id(1L)
                .name("Old")
                .build();
        ItemUpdateDto update = ItemUpdateDto.builder()
                .name("New")
                .build();
        ItemMapper.updateItemFromDto(update, item);
        assertEquals("New", item.getName());
    }

    @Test
    void testUpdateItemRequest() {
        Item item = Item.builder().id(1L).build();
        ItemRequest request = ItemRequest.builder()
                .id(10L)
                .build();
        ItemUpdateDto dto = ItemUpdateDto.builder()
                .requestId(10L)
                .build();
        ItemMapper.updateItemRequest(dto, item, request);
        assertEquals(request, item.getRequest());
    }
}