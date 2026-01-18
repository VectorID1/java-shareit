package ru.practicum.shareit.request.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    ItemRequestRepository requestRepository;

    @Test
    void saveItemRequest() throws Exception {
        User user = userRepository.save(User.builder().name("Alex").email("Alex@mail.ru").build());

        ItemRequest itemRequest = ItemRequest.builder().description("Need Hammer")
                .requester(user)
                .created(LocalDateTime.now())
                .build();

        ItemRequest saveRequest = requestRepository.save(itemRequest);

        assertNotNull(saveRequest.getId());
        assertEquals(user, saveRequest.getRequester());
        assertEquals("Need Hammer", saveRequest.getDescription());
    }

}