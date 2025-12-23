package ru.practicum.shareit.booking.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
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
    public BookingResponseDto createBooking(Long userId, BookingCreateDto bookingCreateDto) {
        User booker = getUserIfExists(userId);

        Item item = getItemIfExists(bookingCreateDto.getItemId());

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь с Id " + item.getId() + " не доступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Владелец вещи не может забронировать свою вещь");
        }
        if (bookingCreateDto.getEnd().isBefore(bookingCreateDto.getStart())) {
            throw new ValidationException("Дата окончания бронирования должна быть позже даты начала бронирования");
        }
        Booking booking = Booking.builder()
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        Booking saveBooking = bookingRepository.save(booking);
        log.info("Создание бронирования с id {} вещи с id = {}, от пользователя с id = {}", booking.getId(), item.getId(), userId);
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
            throw new AccessDeniedException("Нельзя извенить текущий статус бронирования");
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
        log.info("Получение бронирования с Id = {}", bookingId);
        return BookingMapper.toResponseDto(booking);
    }


    @Override
    public List<BookingResponseDto> getUserBooking(Long userId, String state) {
        User user = getUserIfExists(userId);
        BookingState bookingState = BookingState.parseState(state);

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findByBookerOrderByStartDesc(user);
            case CURRENT -> bookingRepository.findCurrentByBooker(user, LocalDateTime.now());
            case PAST -> bookingRepository.findPastByBooker(user, LocalDateTime.now());
            case FUTURE -> bookingRepository.findFutureByBooker(user, LocalDateTime.now());
            case WAITING -> bookingRepository.findByBookerAndStatusOrderByStartDesc(user, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findByBookerAndStatusOrderByStartDesc(user, BookingStatus.REJECTED);
        };
        log.info("Получение всех бронирований пользователя с Id = {}", userId);
        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, String state) {
        User user = getUserIfExists(userId);

        boolean hasItems = itemRepository.existsByOwnerId(userId);

        if (!hasItems) {
            throw new NotFoundException("Пользователь не является владельцем вещей");
        }

        BookingState bookingState = BookingState.parseState(state);

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findByItemOwnerOrderByStartDesc(user);
            case CURRENT -> bookingRepository.findCurrentByItemOwner(user, LocalDateTime.now());
            case PAST -> bookingRepository.findPastByItemOwner(user, LocalDateTime.now());
            case FUTURE -> bookingRepository.findFutureByItemOwner(user, LocalDateTime.now());
            case WAITING -> bookingRepository.findByItemOwnerAndStatusOrderByStartDesc(user, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findByItemOwnerAndStatusOrderByStartDesc(user, BookingStatus.REJECTED);
        };
        log.info("Получение всех бронирований владельца предметов с Id = {}", userId);
        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с Id " + userId + " не найден.");
        }
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с Id " + userId + " не найден."));
    }

    private Item getItemIfExists(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new NotFoundException("Вещь с Id " + itemId + " не найдена"));
    }

    private Booking getBookingIfExists(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new NotFoundException("Бронирование с Id " + bookingId + " не найдено"));
    }
}


