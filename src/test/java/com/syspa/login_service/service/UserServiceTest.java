package com.syspa.login_service.service;

import com.syspa.login_service.model.UserDto;
import com.syspa.login_service.repository.UserRepository;
import com.syspa.login_service.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_UserExists_ReturnsUserDetails() {
        UserDto user = new UserDto();
        user.setUsername("testuser");
        user.setPassword("password");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        UserDetails userDetails = userService.loadUserByUsername("testuser");
        assertEquals("testuser", userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("nouser"));
    }

    @Test
    void save_EncodesPasswordAndSavesUser() {
        UserDto user = new UserDto();
        user.setPassword("plain");
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userRepository.save(any(UserDto.class))).thenReturn(user);
        UserDto saved = userService.save(user);
        assertEquals("encoded", saved.getPassword());
    }

    @Test
    void generateToken_ReturnsToken() {
        UserDto user = new UserDto();
        user.setUsername("user");
        when(jwtUtil.generateToken("user")).thenReturn("token");
        String token = userService.generateToken(user);
        assertEquals("token", token);
    }
}
