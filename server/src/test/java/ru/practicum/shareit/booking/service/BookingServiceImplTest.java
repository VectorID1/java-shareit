package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private User otherUser;
    private Item availableItem;
    private Item unavailableItem;
    private Booking waitingBooking;
    private Booking approvedBooking;

    @BeforeEach
    void setUp() {
        booker = User.builder()
                .id(1L)
                .name("Booker")
                .email("booker@email.com")
                .build();

        owner = User.builder()
                .id(2L)
                .name("Owner")
                .email("owner@email.com")
                .build();

        otherUser = User.builder()
                .id(3L)
                .name("Other")
                .email("other@email.com")
                .build();

        availableItem = Item.builder()
                .id(1L)
                .name("Available Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        unavailableItem = Item.builder()
                .id(2L)
                .name("Unavailable Item")
                .description("Description")
                .available(false)
                .owner(owner)
                .build();

        waitingBooking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .item(availableItem)
                .booker(booker)
                .build();

        approvedBooking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .item(availableItem)
                .booker(booker)
                .build();
    }

    @Test
    void createBookingWhenValidDataThenSuccess() {

        BookingDto bookingDto = BookingDto.builder()
                .itemId(availableItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        Booking bookingToSave = Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking saveBooking = Booking.builder()
                .id(1L)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(availableItem)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(availableItem.getId())).thenReturn(Optional.of(availableItem));
        when(bookingRepository.save(any(Booking.class))).thenReturn(saveBooking);

        BookingResponseDto bookingResponseDto = bookingService.createBooking(booker.getId(), bookingDto);

        assertNotNull(bookingResponseDto);
        verify(bookingRepository).save(argThat(booking ->
                booking.getStatus() == BookingStatus.WAITING &&
                        booking.getItem().equals(availableItem) &&
                        booking.getBooker().equals(booker)));
    }

    @Test
    void createBookingWhenItemNotAvailableThenValidationException() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(unavailableItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(unavailableItem.getId())).thenReturn(Optional.of(unavailableItem));

        ValidationException exception = assertThrows(
                ValidationException.class, () -> bookingService.createBooking(booker.getId(), bookingDto)
        );

        assertEquals("Вещь с id 2 не доступна для бронирования", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingWhenBookerIsOwnerThenAccessDeniedException() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(availableItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(3))
                .build();
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(availableItem.getId())).thenReturn(Optional.of(availableItem));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class, () -> bookingService.createBooking(owner.getId(), bookingDto));

        assertEquals("Владелец вещи не может забронировать свою вещь", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingWhenEndBeforeStartThenValidationException() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(availableItem.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(availableItem.getId())).thenReturn(Optional.of(availableItem));

        ValidationException exception = assertThrows(
                ValidationException.class, () -> bookingService.createBooking(booker.getId(), bookingDto));

        assertEquals("Дата окончания бронирования должна быть позже даты начала бронирования", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBookingWhenUserNotFoundThenNotFoundException() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(availableItem.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> bookingService.createBooking(999L, bookingDto));

        assertEquals("Пользователь с id 999 не найден.", exception.getMessage());
        verify(bookingRepository, never()).save(any());

    }

    @Test
    void createBookingWhenItemNotFoundThenNotFoundException() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(999L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> bookingService.createBooking(booker.getId(), bookingDto));
        assertEquals("Вещь с id 999 не найдена", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void updateBookingStatusWhenOwnerApprovesThenStatusApproved() {
        when(bookingRepository.findById(waitingBooking.getId())).thenReturn(Optional.of(waitingBooking));

        BookingResponseDto bookingResponseDto = bookingService.updateBookingStatus(
                owner.getId(), waitingBooking.getId(), true);

        assertEquals(BookingStatus.APPROVED, bookingResponseDto.getStatus());
        assertNotNull(bookingResponseDto);
    }

    @Test
    void updateBookingStatusWhenOwnerRejectsThenStatusRejected() {
        when(bookingRepository.findById(waitingBooking.getId())).thenReturn(Optional.of(waitingBooking));

        BookingResponseDto bookingResponseDto = bookingService.updateBookingStatus(
                owner.getId(), waitingBooking.getId(), false);

        assertEquals(BookingStatus.REJECTED, bookingResponseDto.getStatus());
        assertNotNull(bookingResponseDto);
    }

    @Test
    void updateBookingStatusWhenUserNotOwnerThenAccessDeniedException() {
        when(bookingRepository.findById(waitingBooking.getId())).thenReturn(Optional.of(waitingBooking));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> bookingService.updateBookingStatus(otherUser.getId(), waitingBooking.getId(), true));
        assertEquals("Только владелец может подтвердить бронирование", exception.getMessage());
    }

    @Test
    void updateBookingStatusWhenStatusNotWaitingThenAccessDeniedException() {
        when(bookingRepository.findById(approvedBooking.getId())).thenReturn(Optional.of(approvedBooking));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> bookingService.updateBookingStatus(owner.getId(), approvedBooking.getId(), true));
        assertEquals("Нельзя изменить текущий статус бронирования", exception.getMessage());
    }

    @Test
    void updateBookingStatusWhenBookingNotFoundThenNotFoundException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.updateBookingStatus(owner.getId(), 999L, true));

        assertEquals("Бронирование с id 999 не найдено", exception.getMessage());
    }

    @Test
    void getBookingByIdWhenUserIsBookerSuccess() {
        when(userRepository.existsById(booker.getId())).thenReturn(true);
        when(bookingRepository.findById(waitingBooking.getId())).thenReturn(Optional.of(waitingBooking));

        BookingResponseDto bookingResponseDto = bookingService.getBookingById(booker.getId(), waitingBooking.getId());

        assertNotNull(bookingResponseDto);
    }

    @Test
    void getBookingByIdWhenUserIsOwnerSuccess() {
        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(bookingRepository.findById(waitingBooking.getId())).thenReturn(Optional.of(waitingBooking));

        BookingResponseDto bookingResponseDto = bookingService.getBookingById(owner.getId(), waitingBooking.getId());

        assertNotNull(bookingResponseDto);
    }

    @Test
    void getBookingByIdWhenUserNotBookerOrOwnerThenAccessDeniedException() {
        when(userRepository.existsById(otherUser.getId())).thenReturn(true);
        when(bookingRepository.findById(waitingBooking.getId())).thenReturn(Optional.of(waitingBooking));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> bookingService.getBookingById(otherUser.getId(), waitingBooking.getId()));

        assertEquals("У вас нет доступа", exception.getMessage());
    }

    @Test
    void getBookingByIdWhenUserNotFoundThenNotFoundException() {

        when(userRepository.existsById(999L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(999L, waitingBooking.getId()));

        assertEquals("Пользователь с id 999 не найден.", exception.getMessage());
    }

    @Test
    void getBookingByIdWhenBookingNotFoundThenNotFoundException() {
        when(userRepository.existsById(booker.getId())).thenReturn(true);
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(booker.getId(), 999L));

        assertEquals("Бронирование с id 999 не найдено", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void getUserBookingWhenDifferentStateThenCallCorrectMethod(BookingState state) {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("start").descending());

        when((userRepository.findById(booker.getId()))).thenReturn(Optional.of(booker));

        switch (state) {
            case ALL -> {
                when(bookingRepository.findByBookerOrderByStartDesc(eq(booker), eq(pageable)))
                        .thenReturn(List.of(waitingBooking));
            }
            case CURRENT -> {
                when(bookingRepository.findCurrentByBooker(eq(booker.getId()), any(LocalDateTime.class), eq(pageable)))
                        .thenReturn(List.of(waitingBooking));
            }
            case PAST -> {
                when(bookingRepository.findPastByBooker(eq(booker.getId()), any(LocalDateTime.class), eq(pageable)))
                        .thenReturn(List.of(waitingBooking));
            }
            case FUTURE -> {
                when(bookingRepository.findFutureByBooker(eq(booker.getId()), any(LocalDateTime.class), eq(pageable)))
                        .thenReturn(List.of(waitingBooking));
            }
            case WAITING -> {
                when(bookingRepository.findByBookerAndStatusOrderByStartDesc(eq(booker), eq(BookingStatus.WAITING), eq(pageable)))
                        .thenReturn(List.of(waitingBooking));
            }
            case REJECTED -> {
                when(bookingRepository.findByBookerAndStatusOrderByStartDesc(eq(booker), eq(BookingStatus.REJECTED), eq(pageable)))
                        .thenReturn(List.of(waitingBooking));
            }
        }

        List<BookingResponseDto> list = bookingService.getUserBooking(booker.getId(), state.name(), 0, 10);

        assertNotNull(list);
        assertFalse(list.isEmpty());

        switch (state) {
            case ALL -> {
                verify(bookingRepository).findByBookerOrderByStartDesc(eq(booker), eq(pageable));
            }
            case CURRENT -> {
                verify(bookingRepository).findCurrentByBooker(eq(booker.getId()), any(LocalDateTime.class), eq(pageable));
            }
            case PAST -> {
                verify(bookingRepository).findPastByBooker(eq(booker.getId()), any(LocalDateTime.class), eq(pageable));
            }
            case FUTURE -> {
                verify(bookingRepository).findFutureByBooker(eq(booker.getId()), any(LocalDateTime.class), eq(pageable));
            }
            case WAITING -> {
                verify(bookingRepository).findByBookerAndStatusOrderByStartDesc(eq(booker), eq(BookingStatus.WAITING), eq(pageable));
            }
            case REJECTED -> {
                verify(bookingRepository).findByBookerAndStatusOrderByStartDesc(eq(booker), eq(BookingStatus.REJECTED), eq(pageable));
            }
        }
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void getBookingOwnerItemsWhenDifferentStatesThenCallCorrectRepositoryMethod(BookingState state) {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("start").descending());

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.existsByOwnerId(owner.getId())).thenReturn(true);

        switch (state) {
            case ALL -> {
                when(bookingRepository.findAllByItemOwnerId(eq(owner.getId()), eq(pageable)))
                        .thenReturn(List.of(waitingBooking));
            }
            case CURRENT -> {
                when(bookingRepository.findCurrentByItemOwner(eq(owner.getId()), any(LocalDateTime.class), eq(pageable)))
                        .thenReturn(List.of(waitingBooking));
            }
            case PAST -> {
                when(bookingRepository.findPastByItemOwner(eq(owner.getId()), any(LocalDateTime.class), eq(pageable)))
                        .thenReturn(List.of(waitingBooking));
            }
            case FUTURE -> {
                when(bookingRepository.findFutureByItemOwner(eq(owner.getId()), any(LocalDateTime.class), eq(pageable)))
                        .thenReturn(List.of(waitingBooking));
            }
            case WAITING -> {
                when(bookingRepository.findByItemOwnerAndStatus(eq(owner), eq(BookingStatus.WAITING), eq(pageable)))
                        .thenReturn(List.of(waitingBooking));
            }
            case REJECTED -> {
                when(bookingRepository.findByItemOwnerAndStatus(eq(owner), eq(BookingStatus.REJECTED), eq(pageable)))
                        .thenReturn(List.of(waitingBooking));
            }
        }

        List<BookingResponseDto> result = bookingService.getBookingOwnerItems(
                owner.getId(), state.name(), 0, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        switch (state) {
            case ALL -> {
                verify(bookingRepository).findAllByItemOwnerId(eq(owner.getId()), eq(pageable));
            }
            case CURRENT -> {
                verify(bookingRepository).findCurrentByItemOwner(eq(owner.getId()), any(LocalDateTime.class), eq(pageable));
            }
            case PAST -> {
                verify(bookingRepository).findPastByItemOwner(eq(owner.getId()), any(LocalDateTime.class), eq(pageable));
            }
            case FUTURE -> {
                verify(bookingRepository).findFutureByItemOwner(eq(owner.getId()), any(LocalDateTime.class), eq(pageable));
            }
            case WAITING -> {
                verify(bookingRepository).findByItemOwnerAndStatus(eq(owner), eq(BookingStatus.WAITING), eq(pageable));
            }
            case REJECTED -> {
                verify(bookingRepository).findByItemOwnerAndStatus(eq(owner), eq(BookingStatus.REJECTED), eq(pageable));
            }
        }
    }

    @Test
    void getUserBookingWhenInvalidStateThenException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.getUserBooking(1L, "INVALID_STATE", 0, 10));

        assertEquals("Неизвестный статус: INVALID_STATE", exception.getMessage());

        verify(userRepository).findById(1L);
    }

    @Test
    void getBookingOwnerItemsWhenUserHasNoItemsThenNotFoundException() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.existsByOwnerId(owner.getId())).thenReturn(false);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getBookingOwnerItems(owner.getId(), "ALL", 0, 10)
        );

        assertEquals("Пользователь не является владельцем вещей", exception.getMessage());
    }

    @Test
    void getUserBookingWhenUserNotFoundThenNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookingService.getUserBooking(999L, "ALL", 0, 10)
        );

        assertEquals("Пользователь с id 999 не найден.", exception.getMessage());
    }
}