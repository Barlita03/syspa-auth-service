package com.syspa.login_service.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        // 32 bytes key for HS256
        String secret = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());
        jwtUtil = new JwtUtil(secret);
    }

    @Test
    void generateAndValidateToken() {
        String token = jwtUtil.generateToken("user");
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() throws InterruptedException {
        // Set expiration to 1 ms for test
        jwtUtil.setExpirationTime(1L);
        String token = jwtUtil.generateToken("user");
        Thread.sleep(5);
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> jwtUtil.validateToken(token));
    }
}
