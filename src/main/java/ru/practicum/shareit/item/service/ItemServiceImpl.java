package ru.practicum.shareit.item.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemResponseDto create(ItemCreateDto itemCreateDto, Long ownerId) {
        Item item = ItemMapper.toItem(itemCreateDto);
        User owner = getUserById(ownerId);
        item.setOwner(owner);

        return ItemMapper.toResponseDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemResponseDto update(Long itemId, ItemUpdateDto itemUpdateDto, Long ownerId) {
        Item exiItem = getItemById(itemId);

        validateOwner(exiItem, ownerId);

        ItemMapper.updateItemFromDto(itemUpdateDto, exiItem);

        return ItemMapper.toResponseDto(exiItem);
    }

    @Override
    public ItemResponseDto getById(Long itemId) {
        Item item = getItemById(itemId);
        return ItemMapper.toResponseDto(item);
    }

    @Override
    public List<ItemResponseDto> search(String text, Long userId) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long itemId) {
        Item item = getItemById(itemId);
        itemRepository.delete(item);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, CommentCreateDto createDto, Long userId) {
        User author = getUserById(userId);
        Item item = getItemById(itemId);

        LocalDateTime now = LocalDateTime.now();
        boolean exists = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(userId, itemId, now);

        if (!exists) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду");
        }

        Comment comment = Comment.builder()
                .text(createDto.getText())
                .item(item)
                .author(author)
                .build();

        Comment saveComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(saveComment);
    }

    @Override
    public ItemResponseDto getByItemId(Long itemId, Long userId) {
        Item item = getItemById(itemId);

        List<CommentDto> comments = commentRepository.findByItemId(itemId)
                .stream()
                .map(CommentMapper::toCommentDto)
                .toList();

        boolean isOwner = item.getOwner().getId().equals(userId);

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;

        if (isOwner) {
            LocalDateTime now = LocalDateTime.now();

            lastBooking = bookingRepository
                    .findLastBookingForItem(itemId, now)
                    .map(BookingMapper::toBookingShortDto)
                    .orElse(null);

            nextBooking = bookingRepository
                    .findNextBookingForItem(itemId, now)
                    .map(BookingMapper::toBookingShortDto)
                    .orElse(null);
        }

        return ItemMapper.toResponseDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemResponseDto> getAllItemsByOwnerId(Long ownerId) {
        List<Item> items = itemRepository.findAllByOwnerId(ownerId);
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        Map<Long, List<CommentDto>> commentsByItemId = commentRepository
                .findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())
                ));

        Map<Long, List<Booking>> bookingsByItemId = bookingRepository
                .findByItem_IdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getItem().getId(),
                        Collectors.toList()
                ));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> enrichItemWithData(
                        item,
                        ownerId,
                        now,
                        bookingsByItemId.getOrDefault(item.getId(), List.of()),
                        commentsByItemId.getOrDefault(item.getId(), List.of())
                ))
                .collect(Collectors.toList());
    }

    private ItemResponseDto enrichItemWithData(
            Item item,
            Long ownerId,
            LocalDateTime now,
            List<Booking> itemBookings,
            List<CommentDto> itemComments) {

        BookingShortDto lastBooking = itemBookings.stream()
                .filter(b -> b.getStart().isBefore(now))
                .max(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::toBookingShortDto)
                .orElse(null);

        BookingShortDto nextBooking = itemBookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::toBookingShortDto)
                .orElse(null);

        return ItemMapper.toResponseDto(item, lastBooking, nextBooking, itemComments);
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
