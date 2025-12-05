package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public Item create(Item item, Long ownerId) {
        User owner = userService.getById(ownerId);
        item.setOwner(owner);

        return itemRepository.save(item);
    }

    @Override
    public Item update(Long itemId, Item item, Long ownerId) {
        Item exiItem = getById(itemId);

        if (!exiItem.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Только владелец вещи может её обновлять.");
        }
        if (item.getName() != null) {
            exiItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            exiItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            exiItem.setAvailable(item.getAvailable());
        }

        return itemRepository.update(exiItem);
    }


    @Override
    public Item getById(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }

    @Override
    public List<Item> getAllByOwner(Long ownerId) {
        userService.getById(ownerId);
        return itemRepository.findAllByOwnerId(ownerId);
    }

    @Override
    public List<Item> search(String text) {
        return itemRepository.search(text);
    }

    @Override
    public void delete(Long itemId) {
        if (!itemRepository.findById(itemId).isPresent()) {
            throw new NullPointerException("Вещь с id " + itemId + " не найдена");
        }
        itemRepository.delete(itemId);
    }
}
