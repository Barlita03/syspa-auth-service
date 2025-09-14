package com.syspa.login_service.service;

import com.syspa.login_service.exceptions.InvalidEmailException;
import com.syspa.login_service.exceptions.InvalidPasswordException;
import com.syspa.login_service.exceptions.InvalidUsernameException;
import com.syspa.login_service.exceptions.NonexistentUserException;
import com.syspa.login_service.model.UserDto;
import com.syspa.login_service.repository.UserRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

  @Autowired private final UserRepository repository;
  @Autowired private final PasswordEncoder passwordEncoder;
  @Autowired private final com.syspa.login_service.utils.JwtUtil jwtUtil;

  @Autowired(required = false)
  private RefreshTokenService refreshTokenService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<UserDto> user = repository.findByUsername(username);
    if (user.isPresent()) {
      var userObj = user.get();
      return User.builder().username(userObj.getUsername()).password(userObj.getPassword()).build();
    } else {
      throw new UsernameNotFoundException("User not found");
    }
  }

  public UserDto save(UserDto user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return repository.save(user);
  }

  public String generateToken(UserDto user) {
    return jwtUtil.generateToken(user.getUsername());
  }

  // --- VALIDATIONS ---

  public void validateUser(UserDto user) {
    Optional<UserDto> foundUser = repository.findByUsername(user.getUsername());
    if (foundUser.isEmpty()) {
      throw new NonexistentUserException("Invalid username or password");
    }

    boolean passwordMatches =
        passwordEncoder.matches(user.getPassword(), foundUser.get().getPassword());
    if (!passwordMatches) {
      throw new NonexistentUserException("Invalid username or password");
    }
  }

  public void validateInput(UserDto user) {
    validateUsername(user.getUsername());
    validateEmail(user.getEmail());
    validatePassword(user.getPassword());
    availableUsername(user.getUsername());
    availableEmail(user.getEmail());
  }

  private void validatePassword(String password) {
    if (password.length() < 8) {
      throw new InvalidPasswordException("Invalid password: must be at least 8 characters long");
    }
  }

  private void validateEmail(String email) {
    String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    if (email.isBlank() || !email.matches(emailRegex)) {
      throw new InvalidEmailException("Invalid email");
    }
  }

  private void validateUsername(String username) {
    if (username.length() < 5) {
      throw new InvalidUsernameException("Invalid username: must be at least 5 characters long");
    }
  }

  private void availableEmail(String email) {
    Optional<UserDto> user = repository.findByEmail(email);
    if (user.isPresent()) {
      throw new InvalidEmailException("The email is already in use");
    }
  }

  private void availableUsername(String username) {
    Optional<UserDto> user = repository.findByUsername(username);
    if (user.isPresent()) {
      throw new InvalidUsernameException("The username is already in use");
    }
  }
}
