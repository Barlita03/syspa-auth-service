package com.syspa.login_service.controller;

import com.syspa.login_service.model.UserDto;
import com.syspa.login_service.model.UserRepository;
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
@RequestMapping("/auth/V0/")
public class RegistrationController {

  @Autowired private final UserRepository repository;

  @Autowired private final PasswordEncoder passwordEncoder;

  @PostMapping("signup")
  public ResponseEntity<UserDto> createUser(@RequestBody UserDto user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    UserDto savedUser = repository.save(user);
    return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
  }
}
