package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.constants.HttpHeadersConstants;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@Slf4j
@Validated
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto createBooking(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long userId,
            @RequestBody BookingDto bookingDto) {
        log.info("POST /bookings - создание бронирования пользователем ID={}", userId);
        return bookingService.createBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateStatusBooking(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long userId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {
        log.info("PATCH /bookings - обновление статуса бронирования ID={}", bookingId);
        return bookingService.updateBookingStatus(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long userId,
            @PathVariable Long bookingId) {
        log.info("GET /bookings - Получение бронирования по Id = {}", bookingId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getBookings(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String state,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {

        log.info("GET /bookings?state={}&from={}&size={} для пользователя {}",
                state, from, size, userId);

        return bookingService.getUserBooking(userId, state, from, size);
    }


    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingOwnerItems(
            @RequestHeader(HttpHeadersConstants.USER_ID_HEADER) Long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String state,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {

        log.info("GET /bookings/owner?state={} для владельца {}",
                state, userId);

        return bookingService.getBookingOwnerItems(userId, state, from, size);
    }
}
