package com.syspa.login_service.Controller;

import com.syspa.login_service.Model.UserDto;
import com.syspa.login_service.Model.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class RegistrationController {

  @Autowired
  private final UserRepository repository;

  @Autowired
  private final PasswordEncoder passwordEncoder;

  @PostMapping("/signup")
  public UserDto createUser(@RequestBody UserDto user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return repository.save(user);
  }
}
