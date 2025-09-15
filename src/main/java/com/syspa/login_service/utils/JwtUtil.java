package com.syspa.login_service.utils;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component

public class JwtUtil {
  public String extractUsername(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }
  private final Key secretKey;
  private long expirationTime = 60 * 60 * 1000; // 1 hour in milliseconds

  public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
    byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32) {
      throw new IllegalArgumentException("JWT secret key must be at least 32 bytes (256 bits) long");
    }
    this.secretKey = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
  }

  public void setExpirationTime(long expirationTime) {
    this.expirationTime = expirationTime;
  }


  public String generateToken(String username, String role) {
    return Jwts.builder()
        .setSubject(username)
        .claim("role", role)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public String extractRole(String token) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
    return claims.get("role", String.class);
  }

  public boolean validateToken(String token) {
    return !isTokenExpired(token);
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
}
