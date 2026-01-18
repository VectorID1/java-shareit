package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    @Transactional
    public ItemRequestDto create(ItemRequestDto requestDto, Long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest itemRequest = ItemRequest.builder()
                .description(requestDto.getDescription())
                .requester(requester)
                .created(LocalDateTime.now())
                .build();
        ItemRequest saveRequest = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.toRequestDtoWithoutItems(saveRequest);
    }

    @Override
    public List<ItemRequestDto> getOwnerRequest(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequesterIdWithItems(userId);

        return itemRequestMapper.toRequestDtoList(requests);
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        ItemRequest request = itemRequestRepository.findByIdWithItems(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден с id: " + requestId));
        return itemRequestMapper.itemRequestDto(request);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (from < 0 || size <= 0) {
            throw new ValidationException("Неверные параметры пагинации");
        }
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());

        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequesterIdNotWithItems(userId, pageable);

        return itemRequestMapper.toRequestDtoList(requests);
    }
}

