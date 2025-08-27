package com.syspa.login_service.controller;

import com.syspa.login_service.model.UserDto;
import com.syspa.login_service.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/auth/V1/")
public class RegistrationController {

  @Autowired private final UserService service;


  @PostMapping("signup")
  public ResponseEntity<?> createUser(@RequestBody UserDto user) {
    service.validateInput(user);
    UserDto savedUser = service.save(user);
    return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
  }


  @PostMapping("login")
  public ResponseEntity<?> logUser(@RequestBody UserDto user) {
    service.validateUser(user);
    String token = service.generateToken(user);
    return new ResponseEntity<>(token, HttpStatus.OK);
  }
}
