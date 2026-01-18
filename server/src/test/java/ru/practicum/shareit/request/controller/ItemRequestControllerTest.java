package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.constants.HttpHeadersConstants.USER_ID_HEADER;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createRequestWhenValidDataThenReturnOk() throws Exception {
        ItemRequestDto inputDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        when(itemRequestService.create(any(ItemRequestDto.class), eq(1L)))
                .thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        verify(itemRequestService).create(any(ItemRequestDto.class), eq(1L));
    }

    @Test
    void getOwnerRequestsWhenValidThenReturnList() throws Exception {
        when(itemRequestService.getOwnerRequest(1L))
                .thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(itemRequestService).getOwnerRequest(1L);
    }

    @Test
    void getRequestByIdWhenValidThenReturnOk() throws Exception {
        Long requestId = 1L;
                Long userId = 1L;
        when(itemRequestService.getRequestById(requestId, userId))
                .thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(itemRequestService).getRequestById(1L, 1L);
    }

    @Test
    void getAllRequestsWhenValidThenReturnList() throws Exception {

        when(itemRequestService.getAllRequests(1L, 0, 5))
                .thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(itemRequestService).getAllRequests(1L, 0, 5);
    }

    @Test
    void getAllRequestsWhenDefaultPaginationThenUseDefaults() throws Exception {
        when(itemRequestService.getAllRequests(eq(1L), eq(0), eq(10)))
                .thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemRequestService).getAllRequests(eq(1L), eq(0), eq(10));
    }

    @Test
    void whenMissingUserIdHeaderThenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isBadRequest());
    }
}