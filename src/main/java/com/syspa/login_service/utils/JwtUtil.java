package com.syspa.login_service.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  private final Key secretKey;
  private long expirationTime = 60 * 60 * 1000; // 1 hour in milliseconds
  private String expectedAudience;
  private String expectedIssuer;

  public JwtUtil(
      @Value("${jwt.secret}") String jwtSecret,
      @Value("${jwt.expectedAudience:}") String expectedAudience,
      @Value("${jwt.expectedIssuer:}") String expectedIssuer) {
    byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32) {
      throw new IllegalArgumentException(
          "JWT secret key must be at least 32 bytes (256 bits) long");
    }
    this.secretKey = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    this.expectedAudience = expectedAudience;
    this.expectedIssuer = expectedIssuer;
  }

  public void setExpirationTime(long expirationTime) {
    this.expirationTime = expirationTime;
  }

  public String generateToken(String username, String role) {
    return Jwts.builder()
        .setSubject(username)
        .claim("role", role)
        .setAudience(
            (expectedAudience != null && !expectedAudience.isBlank()) ? expectedAudience : null)
        .setIssuer((expectedIssuer != null && !expectedIssuer.isBlank()) ? expectedIssuer : null)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public String extractRole(String token) {
    Claims claims =
        Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    return claims.get("role", String.class);
  }

  public boolean validateToken(String token) {
    try {
      Claims claims =
          Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
      if (claims.getExpiration().before(new Date())) return false;
      if (expectedAudience != null && !expectedAudience.isBlank()) {
        if (claims.getAudience() == null || !claims.getAudience().equals(expectedAudience))
          return false;
      }
      if (expectedIssuer != null && !expectedIssuer.isBlank()) {
        if (claims.getIssuer() == null || !claims.getIssuer().equals(expectedIssuer)) return false;
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getExpiration();
  }

  public String extractUsername(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }
}
