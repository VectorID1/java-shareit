package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingDto bookingDto) {
        User booker = getUserIfExists(userId);

        Item item = getItemIfExists(bookingDto.getItemId());

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь с id " + item.getId() + " не доступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Владелец вещи не может забронировать свою вещь");
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new ValidationException("Дата окончания бронирования должна быть позже даты начала бронирования");
        }
        Booking booking = Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        Booking saveBooking = bookingRepository.save(booking);
        log.info("Создание бронирования вещи с id = {}, от пользователя с id = {}",  item.getId(), userId);
        return BookingMapper.toResponseDto(saveBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateBookingStatus(Long userId, Long bookingId, Boolean approved) {

        Booking booking = getBookingIfExists(bookingId);

        Long ownerId = booking.getItem().getOwner().getId();
        if (!ownerId.equals(userId)) {
            throw new AccessDeniedException("Только владелец может подтвердить бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new AccessDeniedException("Нельзя изменить текущий статус бронирования");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.info("Обновление статуса бронирования с id = {}, новый статуст - {}", bookingId, booking.getStatus().toString());
        return BookingMapper.toResponseDto(booking);
    }

    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        validateUserExists(userId);
        Booking booking = getBookingIfExists(bookingId);

        boolean hasAccess = booking.getBooker().getId().equals(userId) ||
                booking.getItem().getOwner().getId().equals(userId);

        if (!hasAccess) {
            throw new AccessDeniedException("У вас нет доступа");
        }
        log.info("Получение бронирования с id = {}", bookingId);
        return BookingMapper.toResponseDto(booking);
    }


    @Override
    public List<BookingResponseDto> getUserBooking(Long userId, String state, Integer from, Integer size) {

        User user = getUserIfExists(userId);
        BookingState bookingState = BookingState.parseState(state);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findByBookerOrderByStartDesc(user, pageable);
            case CURRENT -> bookingRepository.findCurrentByBooker(userId, LocalDateTime.now(), pageable);
            case PAST -> bookingRepository.findPastByBooker(userId, LocalDateTime.now(), pageable);
            case FUTURE -> bookingRepository.findFutureByBooker(userId, LocalDateTime.now(), pageable);
            case WAITING -> bookingRepository.findByBookerAndStatusOrderByStartDesc(user, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findByBookerAndStatusOrderByStartDesc(user, BookingStatus.REJECTED, pageable);
        };
        log.info("Получение всех бронирований пользователя с id = {}", userId);
        return bookings
                .stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<BookingResponseDto> getBookingOwnerItems(Long userId, String state, Integer from, Integer size) {
        User user = getUserIfExists(userId);

        boolean hasItems = itemRepository.existsByOwnerId(userId);

        if (!hasItems) {
            throw new NotFoundException("Пользователь не является владельцем вещей");
        }

        BookingState bookingState = BookingState.parseState(state);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findAllByItemOwnerId(userId, pageable);
            case CURRENT -> bookingRepository.findCurrentByItemOwner(userId, LocalDateTime.now(), pageable);
            case PAST -> bookingRepository.findPastByItemOwner(userId, LocalDateTime.now(), pageable);
            case FUTURE -> bookingRepository.findFutureByItemOwner(userId, LocalDateTime.now(), pageable);
            case WAITING -> bookingRepository.findByItemOwnerAndStatus(user, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findByItemOwnerAndStatus(user, BookingStatus.REJECTED, pageable);
        };
        log.info("Получение всех бронирований владельца предметов с id = {}", userId);
        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден.");
        }
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id " + userId + " не найден."));
    }

    private Item getItemIfExists(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }

    private Booking getBookingIfExists(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new NotFoundException("Бронирование с id " + bookingId + " не найдено"));
    }
}


