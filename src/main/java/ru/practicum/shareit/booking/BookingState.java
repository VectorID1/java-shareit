package ru.practicum.shareit.booking;

import jakarta.validation.ValidationException;

public enum BookingState {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;

    public static BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Неизвестный статус: " + state);
        }
    }
}