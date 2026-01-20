package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private
    ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    void createBookingAndSaveToDatabase() {
        UserDto ownerDto = UserDto.builder()
                .name("Owner")
                .email("owner@mail.ru")
                .build();

        var owner = userService.create(ownerDto);

        ItemDto itemDto = ItemDto.builder()
                .name("Hammer")
                .description("Mini")
                .available(true)
                .build();

        var item = itemService.create(itemDto, owner.getId());

        UserDto bookerDto = UserDto.builder()
                .name("Booker")
                .email("booker@mail.ru")
                .build();

        var booker = userService.create(bookerDto);

        BookingDto bookingDto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingResponseDto result = bookingService.createBooking(booker.getId(), bookingDto);

        assertNotNull(result.getId());

        Optional<Booking> saved = bookingRepository.findById(result.getId());
        assertTrue(saved.isPresent());
        assertEquals("WAITING", saved.get().getStatus().name());
    }
}
