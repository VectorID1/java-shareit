package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemResponseDto create(ItemCreateDto itemCreateDto, Long ownerId) {
        Item item = ItemMapper.toItem(itemCreateDto);
        User owner = getUserById(ownerId);
        item.setOwner(owner);

        return ItemMapper.toResponseDto(itemRepository.save(item));
    }

    @Override
    public ItemResponseDto update(Long itemId, ItemUpdateDto itemUpdateDto, Long ownerId) {
        Item exiItem = getItemById(itemId);

        validateOwner(exiItem, ownerId);

        ItemMapper.updateItemFromDto(itemUpdateDto, exiItem);

        itemRepository.update(exiItem); // в данный момент это лишнее, но когда будет работа с БД эта строка будет нужна!

        return ItemMapper.toResponseDto(exiItem);
    }

    @Override
    public ItemResponseDto getById(Long itemId) {
        Item item = getItemById(itemId);
        return ItemMapper.toResponseDto(item);
    }

    @Override
    public List<ItemResponseDto> getAllByOwner(Long ownerId) {
        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<ItemResponseDto> search(String text) {
        return itemRepository.search(text).stream()
                .map(ItemMapper::toResponseDto)
                .toList();
    }

    @Override
    public void delete(Long itemId) {
        getItemById(itemId);
        itemRepository.delete(itemId);
    }

    private Item getItemById(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователя с id " + userId + "нет"));
    }

    public void validateOwner(Item item, Long user) {
        if (!item.getOwner().getId().equals(user)) {
            throw new AccessDeniedException("Только владелец вещи может её обновлять.");
        }
    }
}
