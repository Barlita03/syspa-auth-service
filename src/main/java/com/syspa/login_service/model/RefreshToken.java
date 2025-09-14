package com.syspa.login_service.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private String token;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false)
  private Instant expiryDate;

  @Column(nullable = false)
  private boolean revoked = false;
}
