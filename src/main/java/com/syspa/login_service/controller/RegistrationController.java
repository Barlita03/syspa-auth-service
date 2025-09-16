package com.syspa.login_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.syspa.login_service.model.AuthResponse;
import com.syspa.login_service.model.LoginRequest;
import com.syspa.login_service.model.RefreshToken;
import com.syspa.login_service.model.RefreshRequest;
import com.syspa.login_service.model.SignupRequest;
import com.syspa.login_service.model.UserDto;
import com.syspa.login_service.service.RefreshTokenService;
import com.syspa.login_service.service.UserService;
import com.syspa.login_service.security.RecaptchaService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/auth/V1/")
public class RegistrationController {

  @Autowired private final UserService service;
  @Autowired private final RefreshTokenService refreshTokenService;
  @Autowired private final RecaptchaService recaptchaService;


  @PostMapping("signup")
  public ResponseEntity<?> createUser(@RequestBody SignupRequest request) {
    if (request.getRecaptchaToken() == null || request.getRecaptchaToken().isBlank()) {
      return new ResponseEntity<>("Missing reCAPTCHA token", HttpStatus.BAD_REQUEST);
    }
    if (!recaptchaService.verify(request.getRecaptchaToken())) {
      return new ResponseEntity<>("Invalid reCAPTCHA", HttpStatus.BAD_REQUEST);
    }
    service.validateInput(request.getUser());
    UserDto savedUser = service.save(request.getUser());
      auditLogger.info("event=USER_REGISTERED, username={}, email={}, id={}", savedUser.getUsername(), savedUser.getEmail(), savedUser.getId());
    return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
  }


  @PostMapping("login")
  public ResponseEntity<?> logUser(@RequestBody LoginRequest request) {
    if (request.getRecaptchaToken() == null || request.getRecaptchaToken().isBlank()) {
      return new ResponseEntity<>("Missing reCAPTCHA token", HttpStatus.BAD_REQUEST);
    }
    if (!recaptchaService.verify(request.getRecaptchaToken())) {
      return new ResponseEntity<>("Invalid reCAPTCHA", HttpStatus.BAD_REQUEST);
    }
  service.validateUser(request.getUser());
  // Recuperar el usuario real de la base para obtener el rol correcto
  UserDto dbUser = service.getByUsername(request.getUser().getUsername());
  String accessToken = service.generateToken(dbUser);
  RefreshToken refreshToken = refreshTokenService.createRefreshToken(dbUser.getUsername());
    auditLogger.info("event=USER_LOGIN, username={}, id={}, role={}", dbUser.getUsername(), dbUser.getId(), dbUser.getRole());
  return new ResponseEntity<>(
    new AuthResponse(accessToken, refreshToken.getToken()), HttpStatus.OK);
  }

  @PostMapping("refresh")
  public ResponseEntity<?> refreshToken(@RequestBody RefreshRequest request) {
    String refreshTokenValue = request.getRefreshToken();
    var tokenOpt = refreshTokenService.findByToken(refreshTokenValue);
    if (tokenOpt.isEmpty() || !refreshTokenService.isValid(tokenOpt.get())) {
        auditLogger.warn("event=REFRESH_TOKEN_INVALID, token={}", refreshTokenValue);
      return new ResponseEntity<>("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
    }
    RefreshToken oldToken = tokenOpt.get();
    refreshTokenService.revokeToken(oldToken); // Rotación: revoca el anterior
    String username = oldToken.getUsername();
    String newAccessToken =
        service.generateToken(
            new UserDto() {{ setUsername(username); }});
    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(username);
      auditLogger.info("event=REFRESH_TOKEN_ROTATED, username={}, oldToken={}, newToken={}", username, oldToken.getToken(), newRefreshToken.getToken());
    return new ResponseEntity<>(
        new AuthResponse(newAccessToken, newRefreshToken.getToken()), HttpStatus.OK);
  }

  // Logger dedicado para auditoría
  private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_LOGGER");
}
