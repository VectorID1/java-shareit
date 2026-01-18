package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemRequest itemRequest;
    private Booking lastBooking;
    private Booking nextBooking;
    private Comment comment;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("Owner@mail.ru")
                .build();

        booker = User.builder()
                .id(2L)
                .name("Booker")
                .email("Booker@mail.ru")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("need item")
                .requester(booker)
                .created(LocalDateTime.now())
                .build();

        lastBooking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingStatus.APPROVED)
                .item(item)
                .booker(booker)
                .build();

        nextBooking = Booking.builder()
                .id(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .item(item)
                .booker(booker)
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("text coment")
                .item(item)
                .author(booker)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createItemWhenValidDataThenSuccess() {
        ItemDto itemDto = ItemDto.builder()
                .name("New item")
                .description("New description")
                .available(true)
                .requestId(1L)
                .build();

        Item itemToSave = ItemMapper.toItem(itemDto);
        itemToSave.setOwner(owner);
        itemToSave.setRequest(itemRequest);

        Item saveItem = Item.builder()
                .id(2L)
                .name("New Item")
                .description("New Description")
                .available(true)
                .owner(owner)
                .request(itemRequest)
                .build();

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(itemDto.getRequestId())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any(Item.class))).thenReturn(saveItem);

        ItemResponseDto itemResponseDto = itemService.create(itemDto, owner.getId());

        assertNotNull(itemResponseDto);
        assertEquals(saveItem.getId(), itemResponseDto.getId());
        assertEquals(saveItem.getName(), itemResponseDto.getName());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItemWhenRequestIdIsNullThenNoRequestAssigned() {
        ItemDto itemDto = ItemDto.builder()
                .name("New item")
                .description("New description")
                .available(true)
                .requestId(null)
                .build();

        Item itemToSave = ItemMapper.toItem(itemDto);
        itemToSave.setOwner(owner);
        itemToSave.setRequest(itemRequest);

        Item saveItem = Item.builder()
                .id(2L)
                .name("New Item")
                .description("New Description")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenReturn(saveItem);

        ItemResponseDto itemResponseDto = itemService.create(itemDto, owner.getId());

        assertNotNull(itemResponseDto);
        assertEquals(saveItem.getId(), itemResponseDto.getId());
        assertEquals(saveItem.getName(), itemResponseDto.getName());
        assertNull(itemResponseDto.getRequestId());


        verify(itemRequestRepository, never()).findById(any());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void createItemWhenUserNotFoundThenNotFoundException() {
        ItemDto itemDto = ItemDto.builder()
                .name("New item")
                .description("New description")
                .available(true)
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> itemService.create(itemDto, 999L));

        assertEquals("Пользователя с id 999 нет", exception.getMessage());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void createItemWhenRequestNotFoundThenNotFoundException() {
        ItemDto itemDto = ItemDto.builder()
                .name("New Item ")
                .description("New description")
                .available(true)
                .requestId(999L)
                .build();

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.create(itemDto, owner.getId()));

        assertEquals("Запрос с id = 999 не найден", exception.getMessage());
        verify(itemRepository, never()).save(any());
    }


    @Test
    void updateItemWhenValidDataThenSuccess() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Updated name")
                .description("Updated description")
                .available(false)
                .requestId(1L)
                .build();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRequestRepository.findById(updateDto.getRequestId())).thenReturn(Optional.of(itemRequest));

        ItemResponseDto itemResponseDto = itemService.update(item.getId(), updateDto, owner.getId());

        assertNotNull(itemResponseDto);

        assertEquals(updateDto.getName(), itemResponseDto.getName());
        assertEquals(updateDto.getDescription(), itemResponseDto.getDescription());
        assertEquals(updateDto.getAvailable(), itemResponseDto.getAvailable());
    }

    @Test
    void updateItemWhenUserNotOwnerThenAccessDeniedException() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("New Name")
                .build();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class, () -> itemService.update(item.getId(), itemUpdateDto, 999L)
        );

        assertEquals("Только владелец вещи может обновлять и удалять предмет.", exception.getMessage());
    }

    @Test
    void updateItemWhenItemNotFoundThenNotFoundException() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("New Name")
                .build();

        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.update(999L, itemUpdateDto, owner.getId()));

        assertEquals("Вещь с id 999 не найдена", exception.getMessage());
    }

    @Test
    void updateItemWhenItemRequestNotFoundThenNotFoundException() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("New Name")
                .requestId(999L)
                .build();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRequestRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.update(item.getId(), itemUpdateDto, owner.getId()));

        assertEquals("Запрос не найден", exception.getMessage());
    }

    @Test
    void getByIdWhenItemExistsThenSuccess() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemResponseDto itemResponseDto = itemService.getById(item.getId());

        assertNotNull(itemResponseDto);
        assertEquals(item.getId(), itemResponseDto.getId());
        assertEquals(item.getName(), itemResponseDto.getName());
    }

    @Test
    void getByIdWhenItemNotFoundThenNotFoundException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.getById(999L));

        assertEquals("Вещь с id 999 не найдена", exception.getMessage());
    }

    @Test
    void getByItemIdWhenOwner() {
        BookingShortDto nextBookingDto = BookingMapper.toBookingShortDto(nextBooking);
        BookingShortDto lastBookingDto = BookingMapper.toBookingShortDto(lastBooking);
        CommentDto commentDto = CommentMapper.toCommentDto(comment);

        when((itemRepository.findById(item.getId()))).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(item.getId())).thenReturn(List.of(comment));
        when(bookingRepository.findNextBookingForItem(eq(item.getId()), any(LocalDateTime.class)))
                .thenReturn(Optional.of(nextBooking));
        when(bookingRepository.findLastBookingForItem(eq(item.getId()), any(LocalDateTime.class)))
                .thenReturn(Optional.of(lastBooking));

        ItemResponseDto itemResponseDto = itemService.getByItemId(item.getId(), owner.getId());

        assertNotNull(itemResponseDto);
        assertEquals(item.getId(), itemResponseDto.getId());
        assertEquals(nextBookingDto, itemResponseDto.getNextBooking());
        assertEquals(lastBookingDto, itemResponseDto.getLastBooking());
        assertFalse(itemResponseDto.getComments().isEmpty());


    }

    @Test
    void getByItemIdWhenNotOwner() {
        CommentDto commentDto = CommentMapper.toCommentDto(comment);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(item.getId())).thenReturn(List.of(comment));

        ItemResponseDto itemResponseDto = itemService.getByItemId(item.getId(), 2L);

        assertNotNull(itemResponseDto);
        assertEquals(item.getId(), itemResponseDto.getId());
        assertNull(itemResponseDto.getNextBooking());
        assertNull(itemResponseDto.getLastBooking());
        assertFalse(itemResponseDto.getComments().isEmpty());
    }

    @Test
    void searchWhenTextNotEmptyThenReturnItems() {
        String searchText = "text";
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());

        when(itemRepository.search(eq(searchText), eq(pageable))).thenReturn(List.of(item));

        List<ItemResponseDto> list = itemService.search(searchText, owner.getId(), 0, 10);

        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        verify(itemRepository).search(eq(searchText), eq(pageable));
    }

    @Test
    void searchWhenTextEmptyThenReturnEmptyList() {
        List<ItemResponseDto> list = itemService.search("", owner.getId(), 0, 10);

        assertNotNull(list);
        assertTrue(list.isEmpty());
        verify(itemRepository, never()).search(anyString(), any(Pageable.class));
    }

    @Test
    void searchWhenTextNullThenReturnEmptyList() {
        List<ItemResponseDto> list = itemService.search(null, owner.getId(), 0, 10);

        assertNotNull(list);
        assertTrue(list.isEmpty());
        verify(itemRepository, never()).search(anyString(), any(Pageable.class));
    }

    @Test
    void getAllItemsByOwnerIdWhenValidThenSuccess() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());

        when(itemRepository.findAllByOwnerId(owner.getId(), pageable)).thenReturn(List.of(item));
        when(commentRepository.findByItemIdIn(anyList())).thenReturn(List.of(comment));
        when(bookingRepository.findByItem_IdIn(anyList())).thenReturn(List.of(lastBooking, nextBooking));

        List<ItemResponseDto> list = itemService.getAllItemsByOwnerId(owner.getId(), 0, 10);

        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
        verify(itemRepository).findAllByOwnerId(eq(owner.getId()), eq(pageable));
    }

    @Test
    void addCommentWhenValidThenSuccess() {
        CommentCreateDto createDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(
                eq(booker.getId()), eq(item.getId()), any(LocalDateTime.class)))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto commentDto = itemService.addComment(item.getId(), createDto, booker.getId());

        assertNotNull(commentDto);
        assertEquals(comment.getText(), commentDto.getText());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addCommentWhenUserDidNotBookItemThenValidationException() {
        CommentCreateDto createDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBefore(
                eq(booker.getId()), eq(item.getId()), any(LocalDateTime.class)))
                .thenReturn(false);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> itemService.addComment(item.getId(), createDto, booker.getId())
        );

        assertEquals("Пользователь не брал эту вещь в аренду", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addCommentWhenItemNotFoundThenNotFoundException() {
        CommentCreateDto createDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.addComment(999L, createDto, booker.getId())
        );

        assertEquals("Вещь с id 999 не найдена", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addCommentWhenUserNotFoundThenNotFoundException() {
        CommentCreateDto createDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> itemService.addComment(item.getId(), createDto, 999L)
        );

        assertEquals("Пользователя с id 999 нет", exception.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void deleteWhenOwnerThenSuccess() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertDoesNotThrow(() -> itemService.delete(item.getId(), owner.getId()));
        verify(itemRepository).delete(item);
    }

    @Test
    void deleteWhenNotOwnerThenAccessDeniedException() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                itemService.delete(item.getId(), 999L));

        assertEquals("Только владелец вещи может обновлять и удалять предмет.", exception.getMessage());
        verify(itemRepository, never()).delete(any());
    }

    @Test
    void deleteWhenItemNotFoundThenNotFoundException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                itemService.delete(999L, owner.getId()));

        assertEquals("Вещь с id 999 не найдена", exception.getMessage());
        verify(itemRepository, never()).delete(any());
    }

    @Test
    void updateItemWhenRequestIdIsZeroThenNoRequestAssigned() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Update name")
                .requestId(0L)
                .build();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemResponseDto itemResponseDto = itemService.update(item.getId(), updateDto, owner.getId());

        assertNotNull(itemResponseDto);
        assertEquals(updateDto.getName(), itemResponseDto.getName());
        verify(itemRequestRepository, never()).findById(any());
    }

    @Test
    void updateItemWhenRequestIdIsNullThenNoRequestAssigned() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Update name")
                .requestId(null)
                .build();

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemResponseDto itemResponseDto = itemService.update(item.getId(), updateDto, owner.getId());

        assertNotNull(itemResponseDto);
        assertEquals(updateDto.getName(), itemResponseDto.getName());
        verify(itemRequestRepository, never()).findById(any());
    }

    @Test
    void getAllItemsByOwnerIdWithPagination() {
        Pageable pageable = PageRequest.of(1, 5, Sort.by("id").descending());

        when(itemRepository.findAllByOwnerId(owner.getId(), pageable)).thenReturn(List.of(item));

        when(commentRepository.findByItemIdIn(anyList())).thenReturn(List.of(comment));

        when(bookingRepository.findByItem_IdIn(anyList())).thenReturn(List.of(lastBooking, nextBooking));

        List<ItemResponseDto> list = itemService.getAllItemsByOwnerId(owner.getId(), 5, 5);

        assertNotNull(list);
        assertFalse(list.isEmpty());
        verify(itemRepository).findAllByOwnerId(owner.getId(), pageable);
    }
}