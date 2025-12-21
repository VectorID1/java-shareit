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

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        Long ownerId = booking.getItem().getOwner().getId();
        if (!ownerId.equals(userId)) {
            throw new AccessDeniedException("Только владелец может подтвердить бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new AccessDeniedException("Нельзя извенить текущий статус бронирования");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        log.info("Обновление статуса бронирования с id = {}, новый статуст - {}", bookingId, booking.getStatus().toString());
        Booking updateStatusBooking = bookingRepository.save(booking);
        return BookingMapper.toResponseDto(updateStatusBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndBookerIdOrOwnerId(bookingId, userId)
                .orElseThrow(() ->
                        new NotFoundException("Бронирование с Id " + bookingId + " не найдено"));
        log.info("Получение бронирования с Id = {}", bookingId);
        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBooking(Long userId, String state) {
        validateUserExists(userId);
        BookingState bookingState = parseState(state);

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findByBookerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository.findCurrentByBooker(userId, LocalDateTime.now());
            case PAST -> bookingRepository.findPastByBooker(userId, LocalDateTime.now());
            case FUTURE -> bookingRepository.findFutureByBooker(userId, LocalDateTime.now());
            case WAITING -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        };
        log.info("Получение всех бронирований пользователя с Id = {}", userId);
        return bookings.stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, String state) {
        validateUserExists(userId);

        boolean hasItems = itemRepository.existsByOwnerId(userId);

        if (!hasItems) {
            throw new NotFoundException("Пользователь не является владельцем вещей");
        }

        BookingState bookingState = parseState(state);

        List<Booking> bookings = switch (bookingState) {
            case ALL -> bookingRepository.findByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT -> bookingRepository.findCurrentByItemOwner(userId, LocalDateTime.now());
            case PAST -> bookingRepository.findPastByItemOwner(userId, LocalDateTime.now());
            case FUTURE -> bookingRepository.findFutureByItemOwner(userId, LocalDateTime.now());
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED ->
                    bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
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

    private BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Неизвестный статус: " + state);
        }
    }
}


