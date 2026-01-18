package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.constants.HttpHeadersConstants.USER_ID_HEADER;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingResponseDto createBookingResponseDto(Long id, BookingStatus status) {
        UserResponseDto booker = UserResponseDto.builder()
                .id(1L)
                .name("booker")
                .email("booker@mail.ru")
                .build();

        ItemResponseDto item = ItemResponseDto.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .build();

        return BookingResponseDto.builder()
                .id(id)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(status)
                .booker(booker)
                .item(item)
                .build();
    }

    private BookingDto createBookingDto() {
        return BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    void createBookingWhenValidDataThenReturnCreated() throws Exception {
        Long userId = 1L;
        BookingDto bookingDto = createBookingDto();
        BookingResponseDto bookingResponseDto = createBookingResponseDto(1L, BookingStatus.WAITING);

        when(bookingService.createBooking(eq(userId), any(BookingDto.class))).thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(1))
                .andExpect(jsonPath("$.item.id").value(1));
    }

    @Test
    void createBookingWhenMissingUserIdHeaderThenReturnBadRequest() throws Exception {
        BookingDto bookingDto = createBookingDto();

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBookingWhenItemNotAvailableThenReturnBadRequest() throws Exception {
        Long userID = 1L;
        BookingDto bookingDto = createBookingDto();

        when(bookingService.createBooking(eq(userID), any(BookingDto.class)))
                .thenThrow(new ValidationException("Вещь с id 1 не доступна для бронирования"));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBookingWhenBookerIsOwnerThenReturnForbidden() throws Exception {
        Long userId = 1L;
        BookingDto bookingDto = createBookingDto();

        when(bookingService.createBooking(eq(userId), any(BookingDto.class)))
                .thenThrow(new AccessDeniedException("Владелец вещи не может забронировать свою вещь"));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isForbidden());
    }

//    @Test
//    void createBookingWhenItemNotFoundThenReturnNotFound() throws Exception {
//        Long userId = 1L;
//        BookingDto bookingDto = createBookingDto();
//
//        when(bookingService.createBooking(eq(userId), any(BookingDto.class)))
//                .thenThrow(new NotFoundException("Вещь с id 1 не найдена"));
//
//        mockMvc.perform(post("/bookings")
//                        .header(USER_ID_HEADER, 1L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(bookingDto)))
//                .andExpect(status().isNotFound());
//
//    }

    @Test
    void updateStatusBookingWhenOwnerApprovesThenReturnApproved() throws Exception {
        Long userid = 2L;
        Long bookingId = 1L;
        Boolean approved = true;

        BookingResponseDto bookingResponseDto = createBookingResponseDto(bookingId, BookingStatus.APPROVED);

        when(bookingService.updateBookingStatus(userid, bookingId, approved)).thenReturn(bookingResponseDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userid)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void updateStatusBookingWhenOwnerRejectsThenReturnRejected() throws Exception {
        Long userid = 2L;
        Long bookingId = 1L;
        Boolean approved = false;

        BookingResponseDto bookingResponseDto = createBookingResponseDto(bookingId, BookingStatus.REJECTED);

        when(bookingService.updateBookingStatus(userid, bookingId, approved)).thenReturn(bookingResponseDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userid)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void updateStatusBookingWhenUserNotOwnerThenReturnForbidden() throws Exception {
        Long userId = 3L; //Не OWNER!
        Long bookingId = 1L;

        when(bookingService.updateBookingStatus(userId, bookingId, true))
                .thenThrow(new AccessDeniedException("Только владелец может подтвердить бронирование"));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatusBookingWhenStatusNotWaitingThenReturnForbidden() throws Exception {
        Long userId = 1L;
        Long bookingId = 1L;

        when(bookingService.updateBookingStatus(userId, bookingId, true))
                .thenThrow(new AccessDeniedException("Нельзя изменить текущий статус бронирования"));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .param("approved", "true"))
                .andExpect(status().isForbidden());

    }

//    @Test
//    void updateStatusBookingWhenBookingNotFoundThenReturnNotFound() throws Exception {
//        Long userId = 1L;
//        Long bookingId = 999L;
//
//        when(bookingService.updateBookingStatus(userId, bookingId, true))
//                .thenThrow(new NotFoundException("Бронирование с id 999 не найдено"));
//
//        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
//                        .header(USER_ID_HEADER, userId)
//                        .param("approved", "true"))
//                .andExpect(status().isNotFound());
//    }

    @Test
    void updateStatusBookingWhenMissingApprovedParamThenReturnBadRequest() throws Exception {
        Long userId = 2L;
        Long bookingId = 1L;

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatusBookingWhenInvalidApprovedParamThenReturnBadRequest() throws Exception {
        Long userId = 2L;
        Long bookingId = 1L;

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId)
                        .param("approved", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingByIdWhenUserIsBookerThenReturnBooking() throws Exception {
        Long userId = 1L;
        Long bookingId = 1L;

        BookingResponseDto bookingResponseDto = createBookingResponseDto(bookingId, BookingStatus.WAITING);

        when(bookingService.getBookingById(userId, bookingId)).thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getBookingByIdWhenUserIsOwnerThenReturnBooking() throws Exception {
        Long owner = 1L;
        Long bookingId = 1L;

        BookingResponseDto bookingResponseDto = createBookingResponseDto(bookingId, BookingStatus.WAITING);

        when(bookingService.getBookingById(owner, bookingId)).thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, owner))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingByIdWhenUserNotBookerOrOwnerThenReturnForbidden() throws Exception {
        Long userId = 3L;
        Long bookingId = 1L;

        when(bookingService.getBookingById(userId, bookingId))
                .thenThrow(new AccessDeniedException("У вас не досутпа"));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isForbidden());
    }

//    @Test
//    void getBookingByIdWhenBookingNotFoundThenReturnNotFound() throws Exception {
//        Long userId = 1L;
//        Long bookingId = 999L;
//
//        when(bookingService.getBookingById(userId, bookingId))
//                .thenThrow(new NotFoundException("Пользователь с id 999 не найден"));
//
//        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
//                        .header(USER_ID_HEADER, userId))
//                .andExpect(status().isNotFound());
//
//    }

    @Test
    void getBookingByIdWhenMissingUserIdHeaderThenReturnBadRequest() throws Exception {
        Long bookingId = 1L;

        mockMvc.perform(get("/bookings/{bookingId}", bookingId))
                .andExpect(status().isBadRequest());

    }

    @Test
    void getBookingsWhenValidRequestThenReturnList() throws Exception {
        Long userId = 1L;
        String state = "ALL";
        Integer from = 0;
        Integer size = 10;

        List<BookingResponseDto> list = List.of(
                createBookingResponseDto(1L, BookingStatus.WAITING),
                createBookingResponseDto(2L, BookingStatus.APPROVED));
        when(bookingService.getUserBooking(userId, state, from, size)).thenReturn(list);

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[1].id").value(2));
    }

    @Test
    void getBookingsWhenNoPaginationParamsThenUseDefaults() throws Exception {
        Long userId = 1L;

        List<BookingResponseDto> list = List.of(
                createBookingResponseDto(1L, BookingStatus.WAITING),
                createBookingResponseDto(2L, BookingStatus.APPROVED),
                createBookingResponseDto(3L, BookingStatus.WAITING));
        when(bookingService.getUserBooking(userId, "ALL", 0, 10)).thenReturn(list);

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getBookingsWhenInvalidStateThenReturnBadRequest() throws Exception {
        Long userId = 1L;
        String invalidState = "INVALID_STATE";

        when(bookingService.getUserBooking(userId, invalidState, 0, 10))
                .thenThrow(new ValidationException("Неизвесстный статус " + invalidState));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .param("state", invalidState))
                .andExpect(status().isBadRequest());
    }

//    @Test
//    void getBookingsWhenUserNotFoundThenReturnNotFound() throws Exception {
//        Long userId = 999L;
//
//        when(bookingService.getUserBooking(userId, "ALL", 0, 10))
//                .thenThrow(new NotFoundException("Пользователя с id 999 не найден"));
//
//        mockMvc.perform(get("/bookings")
//                        .header(USER_ID_HEADER, userId))
//                .andExpect(status().isNotFound());
//    }

    @Test
    void getBookingOwnerItemsWhenValidRequestThenReturnList() throws Exception {
        Long owner = 1L;
        String state = "ALL";

        List<BookingResponseDto> list = List.of(
                createBookingResponseDto(1L, BookingStatus.WAITING),
                createBookingResponseDto(2L, BookingStatus.WAITING));

        when(bookingService.getBookingOwnerItems(owner, state, 0, 10)).thenReturn(list);

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, owner)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

    }

    @Test
    void getBookingOwnerItemsWhenUserHasNoItemsThenReturnNotFound() throws Exception {
        Long userId = 2L;

        when(bookingService.getBookingOwnerItems(userId, "ALL", 0, 10))
                .thenThrow(new NotFoundException("Пользователь не является владельцем вещей"));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isNotFound());
    }

//    @Test
//    void getBookingOwnerItemsWhenUserNotFoundThenReturnNotFound() throws Exception {
//        Long userId = 999L;
//
//        when(bookingService.getBookingOwnerItems(userId, "ALL", 0, 10))
//                .thenThrow(new NotFoundException("Пользователь с id 999  не найден"));
//
//        mockMvc.perform(get("/bookings/owner")
//                        .header(USER_ID_HEADER, userId))
//                .andExpect(status().isNotFound());
//    }

//    @Test
//    void getBookingOwnerItemsWhenInvalidStateThenReturnBadRequest() throws Exception {
//        Long userId = 2L;
//        String invalidState = "INVALID_STATE";
//
//        when(bookingService.getBookingOwnerItems(userId, invalidState, 0, 10))
//                .thenThrow(new ValidationException("Неизвестный стаус: " + invalidState));
//
//        mockMvc.perform(get("/bookings/owner")
//                        .header(USER_ID_HEADER, userId)
//                        .param("state", invalidState))
//                .andExpect(status().isBadRequest());
//    }

    @Test
    void getBookingOwnerItemsWhenCustomPaginationThenUseParams() throws Exception {
        Long userId = 1L;
        Integer from = 5;
        Integer size = 15;

        List<BookingResponseDto> list = List.of(
                createBookingResponseDto(1L, BookingStatus.WAITING));

        when(bookingService.getBookingOwnerItems(userId, "ALL", from, size)).thenReturn(list);

        mockMvc.perform(get("/bookings/owner", userId)
                        .header(USER_ID_HEADER, userId)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isOk());

        verify(bookingService).getBookingOwnerItems(userId, "ALL", from, size);
    }
}