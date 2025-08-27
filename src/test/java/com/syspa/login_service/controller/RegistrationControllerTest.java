package com.syspa.login_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syspa.login_service.model.UserDto;
import com.syspa.login_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@WebMvcTest(RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {RegistrationController.class, RegistrationControllerTest.TestSecurityConfig.class})
class RegistrationControllerTest {
    @Configuration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf().disable().authorizeHttpRequests().anyRequest().permitAll();
            return http.build();
        }
    }
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createUser_ValidInput_ReturnsCreated() throws Exception {
        UserDto user = new UserDto();
        user.setUsername("user1");
        user.setPassword("password123");
        user.setEmail("user1@email.com");
        Mockito.doNothing().when(userService).validateInput(any(UserDto.class));
        Mockito.when(userService.save(any(UserDto.class))).thenReturn(user);
        mockMvc.perform(post("/auth/V1/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void logUser_ValidInput_ReturnsOk() throws Exception {
        UserDto user = new UserDto();
        user.setUsername("user1");
        user.setPassword("password123");
        Mockito.doNothing().when(userService).validateUser(any(UserDto.class));
        Mockito.when(userService.generateToken(any(UserDto.class))).thenReturn("token");
        mockMvc.perform(post("/auth/V1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }
}
