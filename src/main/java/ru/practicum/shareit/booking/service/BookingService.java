package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;


public interface BookingService {
    BookingResponseDto createBooking(Long userId, BookingCreateDto bookingCreateDto);

    BookingResponseDto updateBookingStatus(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto getBookingById(Long userId, Long bookingId);

    List<BookingResponseDto> getUserBooking(Long userId, String state);

    List<BookingResponseDto> getOwnerBookings(Long userId, String state);
}

