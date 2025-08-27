package com.syspa.login_service.repository;

import com.syspa.login_service.model.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindByUsername() {
        UserDto user = new UserDto();
        user.setUsername("repoUser");
        user.setPassword("password");
        user.setEmail("repo@email.com");
        userRepository.save(user);
        Optional<UserDto> found = userRepository.findByUsername("repoUser");
        assertTrue(found.isPresent());
        assertEquals("repoUser", found.get().getUsername());
    }

    @Test
    void findByEmail() {
        UserDto user = new UserDto();
        user.setUsername("repoUser2");
        user.setPassword("password");
        user.setEmail("repo2@email.com");
        userRepository.save(user);
        Optional<UserDto> found = userRepository.findByEmail("repo2@email.com");
        assertTrue(found.isPresent());
        assertEquals("repo2@email.com", found.get().getEmail());
    }
}
