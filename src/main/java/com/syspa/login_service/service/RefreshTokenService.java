package com.syspa.login_service.service;

import org.springframework.transaction.annotation.Transactional;
import com.syspa.login_service.model.RefreshToken;
import com.syspa.login_service.repository.RefreshTokenRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RefreshTokenService {
  @Autowired private final RefreshTokenRepository refreshTokenRepository;

  private final long refreshTokenDurationMs = 7 * 24 * 60 * 60 * 1000L; // 7 días

  @Transactional
  public RefreshToken createRefreshToken(String username) {
    // Revoca tokens anteriores
    refreshTokenRepository.deleteByUsername(username);
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUsername(username);
    refreshToken.setToken(generateRandomToken());
    refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
    refreshToken.setRevoked(false);
    return refreshTokenRepository.save(refreshToken);
  }

  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token);
  }

  public boolean isValid(RefreshToken token) {
    return !token.isRevoked() && token.getExpiryDate().isAfter(Instant.now());
  }

  public void revokeToken(RefreshToken token) {
    token.setRevoked(true);
    refreshTokenRepository.save(token);
  }

  private String generateRandomToken() {
    byte[] randomBytes = new byte[64];
    new SecureRandom().nextBytes(randomBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }
}
