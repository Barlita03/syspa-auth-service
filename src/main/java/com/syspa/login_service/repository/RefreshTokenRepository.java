package com.syspa.login_service.repository;

import com.syspa.login_service.model.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByToken(String token);

  void deleteByUsername(String username);

  void deleteAllByExpiryDateBefore(Instant expiryDate);
}
