package com.syspa.login_service.service;

import com.syspa.login_service.model.PasswordResetToken;
import com.syspa.login_service.model.UserDto;
import com.syspa.login_service.repository.PasswordResetTokenRepository;
import com.syspa.login_service.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PasswordResetService {
    @Autowired private final PasswordResetTokenRepository tokenRepository;
    @Autowired private final UserRepository userRepository;
    @Autowired private final PasswordEncoder passwordEncoder;

    private final long tokenDurationMs = 15 * 60 * 1000L; // 15 minutos

    @Transactional
    public PasswordResetToken createToken(String username) {
        tokenRepository.deleteAllByExpiryDateBefore(Instant.now());
        PasswordResetToken token = new PasswordResetToken();
        token.setUsername(username);
        token.setToken(generateRandomToken());
        token.setExpiryDate(Instant.now().plusMillis(tokenDurationMs));
        token.setUsed(false);
        return tokenRepository.save(token);
    }

    public boolean isValid(PasswordResetToken token) {
        return !token.isUsed() && token.getExpiryDate().isAfter(Instant.now());
    }

    @Transactional
    public boolean resetPassword(String tokenValue, String newPassword) {
        var tokenOpt = tokenRepository.findByToken(tokenValue);
        if (tokenOpt.isEmpty()) return false;
        PasswordResetToken token = tokenOpt.get();
        if (!isValid(token)) return false;
        var userOpt = userRepository.findByUsername(token.getUsername());
        if (userOpt.isEmpty()) return false;
        UserDto user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        token.setUsed(true);
        tokenRepository.save(token);
        return true;
    }

    // Purga automática de tokens vencidos cada hora
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void purgeExpiredTokens() {
        tokenRepository.deleteAllByExpiryDateBefore(Instant.now());
    }

    private String generateRandomToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
