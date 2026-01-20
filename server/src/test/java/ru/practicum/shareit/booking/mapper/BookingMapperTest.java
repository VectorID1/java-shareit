package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    @Test
    void instantiateMapper() {
        BookingMapper mapper = new BookingMapper();
        assertNotNull(mapper);
    }

    @Test
    void toBookingResponse_whenValidDto_thenSuccess() {
        User owner = User.builder()
                .id(1L)
                .name("Alex")
                .email("Alex@email.com")
                .build();
        User booker = User.builder()
                .id(2L)
                .name("AlexB")
                .email("AlexB@email.com")
                .build();
        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .description("descr")
                .owner(owner)
                .available(true)
                .build();

        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .created(LocalDateTime.now())
                .build();

        BookingResponseDto bookingResponseDto = BookingMapper.toResponseDto(booking);

        assertEquals(1L, bookingResponseDto.getId());
        assertEquals(booking.getStart(), bookingResponseDto.getStart());
        assertEquals(booking.getEnd(), bookingResponseDto.getEnd());
        assertEquals(BookingStatus.WAITING, bookingResponseDto.getStatus());
        assertEquals(booking.getCreated(), bookingResponseDto.getCreated());
    }
}