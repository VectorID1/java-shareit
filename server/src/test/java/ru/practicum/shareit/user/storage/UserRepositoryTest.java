package ru.practicum.shareit.user.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;


import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void saveUser() {
        User user1 = User.builder()
                .name("Alex")
                .email("Alex@mail.ru")
                .build();

        User userSave = userRepository.save(user1);


        assertNotNull(userSave.getId());
    }


}