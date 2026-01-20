package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;


public interface BookingService {

    BookingResponseDto createBooking(Long userId, BookingDto bookingDto);

    BookingResponseDto updateBookingStatus(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto getBookingById(Long userId, Long bookingId);

    List<BookingResponseDto> getUserBooking(Long userId, String state, Integer from, Integer size);

    List<BookingResponseDto> getBookingOwnerItems(Long userId, String state, Integer from, Integer size);

}

