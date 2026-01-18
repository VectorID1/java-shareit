package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User requester;
    private User otherUser;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        requester = User.builder()
                .id(1L)
                .name("Requester")
                .email("requester@email.com")
                .build();

        otherUser = User.builder()
                .id(2L)
                .name("Other User")
                .email("other@email.com")
                .build();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Need a drill")
                .requester(requester)
                .created(LocalDateTime.now())
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createRequestWhenValidDataThenSuccess() {
        ItemRequestDto inputDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        ItemRequestDto outputDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();

        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);
        when(itemRequestMapper.toRequestDtoWithoutItems(itemRequest)).thenReturn(outputDto);

        ItemRequestDto result = itemRequestService.create(inputDto, requester.getId());

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Need a drill", result.getDescription());
        verify(itemRequestRepository).save(any(ItemRequest.class));
        verify(itemRequestMapper).toRequestDtoWithoutItems(itemRequest);
    }

//    @Test
//    void createRequestWhenUserNotFoundThenNotFoundException() {
//        ItemRequestDto inputDto = ItemRequestDto.builder()
//                .description("Need a drill")
//                .build();
//
//        when(userRepository.findById(999L)).thenReturn(Optional.empty());
//
//        NotFoundException exception = assertThrows(
//                NotFoundException.class,
//                () -> itemRequestService.create(inputDto, 999L)
//        );
//
//        assertEquals("Пользователь не найден", exception.getMessage());
//        verify(itemRequestRepository, never()).save(any());
//    }

    @Test
    void getOwnerRequestWhenValidThenReturnList() {
        when(userRepository.existsById(requester.getId())).thenReturn(true);
        when(itemRequestRepository.findAllByRequesterIdWithItems(requester.getId()))
                .thenReturn(List.of(itemRequest));
        when(itemRequestMapper.toRequestDtoList(List.of(itemRequest)))
                .thenReturn(List.of(itemRequestDto));

        List<ItemRequestDto> result = itemRequestService.getOwnerRequest(requester.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userRepository).existsById(requester.getId());
        verify(itemRequestRepository).findAllByRequesterIdWithItems(requester.getId());
    }

//    @Test
//    void getOwnerRequestWhenUserNotFoundThenNotFoundException() {
//        when(userRepository.existsById(999L)).thenReturn(false);
//
//        NotFoundException exception = assertThrows(
//                NotFoundException.class,
//                () -> itemRequestService.getOwnerRequest(999L)
//        );
//
//        assertEquals("Пользователь не найден", exception.getMessage());
//        verify(itemRequestRepository, never()).findAllByRequesterIdWithItems(anyLong());
//    }

    @Test
    void getRequestByIdWhenValidThenSuccess() {
        when(userRepository.existsById(requester.getId())).thenReturn(true);
        when(itemRequestRepository.findByIdWithItems(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));
        when(itemRequestMapper.itemRequestDto(itemRequest)).thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.getRequestById(itemRequest.getId(), requester.getId());

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).existsById(requester.getId());
        verify(itemRequestRepository).findByIdWithItems(itemRequest.getId());
        verify(itemRequestMapper).itemRequestDto(itemRequest);
    }

//    @Test
//    void getRequestByIdWhenUserNotFoundThenNotFoundException() {
//        when(userRepository.existsById(999L)).thenReturn(false);
//
//        NotFoundException exception = assertThrows(
//                NotFoundException.class,
//                () -> itemRequestService.getRequestById(1L, 999L)
//        );
//
//        assertEquals("Пользователь не найден", exception.getMessage());
//        verify(itemRequestRepository, never()).findByIdWithItems(anyLong());
//    }
//
//    @Test
//    void getRequestByIdWhenRequestNotFoundThenNotFoundException() {
//        when(userRepository.existsById(requester.getId())).thenReturn(true);
//        when(itemRequestRepository.findByIdWithItems(999L)).thenReturn(Optional.empty());
//
//        NotFoundException exception = assertThrows(
//                NotFoundException.class,
//                () -> itemRequestService.getRequestById(999L, requester.getId())
//        );
//
//        assertEquals("Запрос не найден с id: 999", exception.getMessage());
//        verify(itemRequestRepository).findByIdWithItems(999L);
//    }

    @Test
    void getAllRequestsWhenValidThenReturnList() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").descending());

        when(userRepository.existsById(requester.getId())).thenReturn(true);
        when(itemRequestRepository.findAllByRequesterIdNotWithItems(eq(requester.getId()), eq(pageable)))
                .thenReturn(List.of(itemRequest));
        when(itemRequestMapper.toRequestDtoList(List.of(itemRequest)))
                .thenReturn(List.of(itemRequestDto));

        List<ItemRequestDto> result = itemRequestService.getAllRequests(requester.getId(), 0, 10);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userRepository).existsById(requester.getId());
        verify(itemRequestRepository).findAllByRequesterIdNotWithItems(eq(requester.getId()), eq(pageable));
    }

//    @Test
//    void getAllRequestsWhenUserNotFoundThenNotFoundException() {
//        when(userRepository.existsById(999L)).thenReturn(false);
//
//        NotFoundException exception = assertThrows(
//                NotFoundException.class,
//                () -> itemRequestService.getAllRequests(999L, 0, 10)
//        );
//
//        assertEquals("Пользователь не найден", exception.getMessage());
//        verify(itemRequestRepository, never()).findAllByRequesterIdNotWithItems(anyLong(), any());
//    }
//
//    @Test
//    void getAllRequestsWhenFromNegativeThenValidationException() {
//        when(userRepository.existsById(requester.getId())).thenReturn(true);
//
//        ValidationException exception = assertThrows(
//                ValidationException.class,
//                () -> itemRequestService.getAllRequests(requester.getId(), -1, 10)
//        );
//
//        assertEquals("Неверные параметры пагинации", exception.getMessage());
//        verify(itemRequestRepository, never()).findAllByRequesterIdNotWithItems(anyLong(), any());
//    }
//
//    @Test
//    void getAllRequestsWhenSizeZeroThenValidationException() {
//        when(userRepository.existsById(requester.getId())).thenReturn(true);
//
//        ValidationException exception = assertThrows(
//                ValidationException.class,
//                () -> itemRequestService.getAllRequests(requester.getId(), 0, 0)
//        );
//
//        assertEquals("Неверные параметры пагинации", exception.getMessage());
//        verify(itemRequestRepository, never()).findAllByRequesterIdNotWithItems(anyLong(), any());
//    }
//
//    @Test
//    void getAllRequestsWhenSizeNegativeThenValidationException() {
//        when(userRepository.existsById(requester.getId())).thenReturn(true);
//
//        ValidationException exception = assertThrows(
//                ValidationException.class,
//                () -> itemRequestService.getAllRequests(requester.getId(), 0, -1)
//        );
//
//        assertEquals("Неверные параметры пагинации", exception.getMessage());
//        verify(itemRequestRepository, never()).findAllByRequesterIdNotWithItems(anyLong(), any());
//    }

    @Test
    void getAllRequestsWithPaginationCalculation() {
        // from=10, size=5 -> page=10/5=2
        Pageable pageable = PageRequest.of(2, 5, Sort.by("created").descending());

        when(userRepository.existsById(requester.getId())).thenReturn(true);
        when(itemRequestRepository.findAllByRequesterIdNotWithItems(eq(requester.getId()), eq(pageable)))
                .thenReturn(List.of(itemRequest));
        when(itemRequestMapper.toRequestDtoList(List.of(itemRequest)))
                .thenReturn(List.of(itemRequestDto));

        List<ItemRequestDto> result = itemRequestService.getAllRequests(requester.getId(), 10, 5);

        assertNotNull(result);
        verify(itemRequestRepository).findAllByRequesterIdNotWithItems(eq(requester.getId()), eq(pageable));
    }

    @Test
    void getAllRequestsWhenZeroFromAndSizeThenValid() {
        Pageable pageable = PageRequest.of(0, 1, Sort.by("created").descending());

        when(userRepository.existsById(requester.getId())).thenReturn(true);
        when(itemRequestRepository.findAllByRequesterIdNotWithItems(eq(requester.getId()), eq(pageable)))
                .thenReturn(List.of(itemRequest));
        when(itemRequestMapper.toRequestDtoList(List.of(itemRequest)))
                .thenReturn(List.of(itemRequestDto));

        List<ItemRequestDto> result = itemRequestService.getAllRequests(requester.getId(), 0, 1);

        assertNotNull(result);
        verify(itemRequestRepository).findAllByRequesterIdNotWithItems(eq(requester.getId()), eq(pageable));
    }

    @Test
    void getAllRequestsWhenEmptyResultThenReturnEmptyList() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("created").descending());

        when(userRepository.existsById(requester.getId())).thenReturn(true);
        when(itemRequestRepository.findAllByRequesterIdNotWithItems(eq(requester.getId()), eq(pageable)))
                .thenReturn(List.of());
        when(itemRequestMapper.toRequestDtoList(List.of()))
                .thenReturn(List.of());

        List<ItemRequestDto> result = itemRequestService.getAllRequests(requester.getId(), 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRequestRepository).findAllByRequesterIdNotWithItems(eq(requester.getId()), eq(pageable));
    }

    @Test
    void createRequestShouldSetCurrentTime() {
        ItemRequestDto inputDto = ItemRequestDto.builder()
                .description("Need item")
                .build();

        LocalDateTime beforeTest = LocalDateTime.now().minusSeconds(1);

        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenAnswer(invocation -> {
            ItemRequest saved = invocation.getArgument(0);
            assertNotNull(saved.getCreated());
            assertTrue(saved.getCreated().isAfter(beforeTest) || saved.getCreated().equals(beforeTest));
            return itemRequest;
        });
        when(itemRequestMapper.toRequestDtoWithoutItems(itemRequest)).thenReturn(itemRequestDto);

        itemRequestService.create(inputDto, requester.getId());

        verify(itemRequestRepository).save(any(ItemRequest.class));
    }
}