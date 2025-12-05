package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item create(Item item, Long ownerId);
    Item update(Long itemId, Item item, Long ownerId);
    Item getById(Long itemId);
    List<Item> getAllByOwner(Long ownerId);
    List<Item> search(String text);
    void delete(Long itemId);
}
