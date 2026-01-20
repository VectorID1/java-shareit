package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.constants.HttpHeadersConstants.USER_ID_HEADER;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private ItemUpdateDto itemUpdateDto;
    private ItemResponseDto itemResponseDto;
    private CommentCreateDto createDto;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .requestId(1L)
                .build();

        itemUpdateDto = ItemUpdateDto.builder()
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .requestId(2L)
                .build();

        itemResponseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        createDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();

        commentDto = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("Booker")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createItemWhenValidDataThenReturnOk() throws Exception {
        when(itemService.create(any(ItemDto.class), eq(1L))).thenReturn(itemResponseDto);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"));

        verify(itemService).create(any(ItemDto.class), eq(1L));
    }

    @Test
    void updateItemWhenValidDataThenReturnOk() throws Exception {
        Long itemId = 1L;
        when(itemService.update(eq(1L), any(ItemUpdateDto.class), eq(1L))).thenReturn(itemResponseDto);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId));
        verify(itemService).update(eq(itemId), any(ItemUpdateDto.class), eq(1L));
    }

    @Test
    void getItemByIdWhenValidThenReturnOk() throws Exception {
        Long itemId = 1L;

        when(itemService.getByItemId(itemId, 1L)).thenReturn(itemResponseDto);

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
        verify(itemService).getByItemId(itemId, 1L);
    }

    @Test
    void getAllItemsByOwnerWhenValidThenReturnList() throws Exception {
        when(itemService.getAllItemsByOwnerId(1L, 0, 6)).thenReturn(List.of(itemResponseDto));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(itemService).getAllItemsByOwnerId(1L, 0, 6);
    }

    @Test
    void getAllItemsByOwnerWhenDefaultPaginationThenUseDefaults() throws Exception {
        when(itemService.getAllItemsByOwnerId(1L, 0, 10))
                .thenReturn(List.of(itemResponseDto));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemService).getAllItemsByOwnerId(1L, 0, 10);
    }

    @Test
    void searchItemsWhenValidTextThenReturnList() throws Exception {
        when(itemService.search("test", 1L, 0, 5))
                .thenReturn(List.of(itemResponseDto));

        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1L)
                        .param("text", "test")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(itemService).search("test", 1L, 0, 5);
    }

    @Test
    void searchItemsWhenDefaultPaginationThenUseDefaults() throws Exception {
        when(itemService.search("test", 1L, 0, 10))
                .thenReturn(List.of(itemResponseDto));

        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1L)
                        .param("text", "test"))
                .andExpect(status().isOk());

        verify(itemService).search("test", 1L, 0, 10);
    }

    @Test
    void deleteItemWhenOwnerThenReturnOk() throws Exception {
        Long itemId = 1L;
        Long ownerId = 1L;
        doNothing().when(itemService).delete(itemId, ownerId);

        mockMvc.perform(delete("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isOk());
    }

    @Test
    void addCommentWhenValidThenReturnOk() throws Exception {
        Long itemId = 1L;
        Long bookerId = 1L;
        when(itemService.addComment(eq(itemId), any(CommentCreateDto.class), eq(bookerId)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"));

        verify(itemService).addComment(eq(itemId), any(CommentCreateDto.class), eq(bookerId));
    }

    @Test
    void updateItemWithPartialDataThenReturnOk() throws Exception {
        Long itemId = 1L;
        Long ownerId = 1L;

        ItemUpdateDto partialUpdate = ItemUpdateDto.builder()
                .name("Only Name Updated")
                .build();

        ItemResponseDto updatedResponse = ItemResponseDto.builder()
                .id(1L)
                .name("Only Name Updated")
                .description("Test Description")
                .available(true)
                .build();

        when(itemService.update(eq(itemId), any(ItemUpdateDto.class), eq(ownerId)))
                .thenReturn(updatedResponse);

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Only Name Updated"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(itemService).update(eq(itemId), any(ItemUpdateDto.class), eq(ownerId));
    }

    @Test
    void whenMissingUserIdHeaderThenReturnBadRequest() throws Exception {
        Long itemId = 1L;
        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenMissingRequiredParamThenReturnBadRequest() throws Exception {
        Long bookerId = 1L;
        mockMvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, bookerId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenInvalidPathParamFormatThenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/not-a-number")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenServiceThrowsNotFoundExceptionThenReturnNotFound() throws Exception {
        when(itemService.getByItemId(999L, 1L))
                .thenThrow(new NotFoundException("Вещь с id " + 999L + " не найдена"));

        mockMvc.perform(get("/items/999")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isNotFound());
    }

}