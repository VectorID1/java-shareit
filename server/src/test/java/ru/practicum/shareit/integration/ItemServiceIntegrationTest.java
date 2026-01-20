package ru.practicum.shareit.integration;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ItemServiceIntegrationTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepository itemRepository;

    @Test
    void createItemAndSaveToDataBase() {
        UserDto userDto = UserDto.builder()
                .name("Owner")
                .email("Owner@mail.ru")
                .build();
        var owner = userService.create(userDto);

        ItemDto itemDto = ItemDto.builder()
                .name("Hammer")
                .description("mini")
                .available(true)
                .build();

        ItemResponseDto itemResponseDto = itemService.create(itemDto, owner.getId());

        assertNotNull(itemResponseDto);

        Optional<Item> item = itemRepository.findById(itemResponseDto.getId());
        assertTrue(item.isPresent());
        assertEquals("Hammer", item.get().getName());
        assertEquals(owner.getId(), item.get().getOwner().getId());

    }

    @Test
    void updateItemAndSaveToDataBase() {
        UserDto userDto = UserDto.builder()
                .name("Owner")
                .email("Owner@mail.ru")
                .build();
        var owner = userService.create(userDto);

        ItemDto itemDto = ItemDto.builder()
                .name("Hammer")
                .description("mini")
                .available(true)
                .build();

        ItemUpdateDto update = ItemUpdateDto.builder()
                .description("Max")
                .build();
        ItemResponseDto itemResponseDto = itemService.create(itemDto, owner.getId());

        assertNotNull(itemResponseDto);

        ItemResponseDto updateItem = itemService.update(itemResponseDto.getId(), update, owner.getId());

        assertNotNull(updateItem);

        Optional<Item> item = itemRepository.findById(updateItem.getId());
        assertTrue(item.isPresent());
        assertEquals("Max", item.get().getDescription());
    }
}
