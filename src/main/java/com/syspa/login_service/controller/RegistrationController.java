package com.syspa.login_service.controller;

import com.syspa.login_service.model.AuthResponse;
import com.syspa.login_service.model.RefreshToken;
import com.syspa.login_service.model.RefreshRequest;
import com.syspa.login_service.model.UserDto;
import com.syspa.login_service.service.RefreshTokenService;
import com.syspa.login_service.service.UserService;
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

  @PostMapping("signup")
  public ResponseEntity<?> createUser(@RequestBody UserDto user) {
    service.validateInput(user);
    UserDto savedUser = service.save(user);
    return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
  }

  @PostMapping("login")
  public ResponseEntity<?> logUser(@RequestBody UserDto user) {
    service.validateUser(user);
    String accessToken = service.generateToken(user);
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
    return new ResponseEntity<>(
        new AuthResponse(accessToken, refreshToken.getToken()), HttpStatus.OK);
  }

  @PostMapping("refresh")
  public ResponseEntity<?> refreshToken(@RequestBody RefreshRequest request) {
    String refreshTokenValue = request.getRefreshToken();
    var tokenOpt = refreshTokenService.findByToken(refreshTokenValue);
    if (tokenOpt.isEmpty() || !refreshTokenService.isValid(tokenOpt.get())) {
      return new ResponseEntity<>("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
    }
    RefreshToken oldToken = tokenOpt.get();
    refreshTokenService.revokeToken(oldToken); // Rotación: revoca el anterior
    String username = oldToken.getUsername();
    String newAccessToken =
        service.generateToken(
            new UserDto() {{ setUsername(username); }});
    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(username);
    return new ResponseEntity<>(
        new AuthResponse(newAccessToken, newRefreshToken.getToken()), HttpStatus.OK);
  }
}
