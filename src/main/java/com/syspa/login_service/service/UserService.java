package com.syspa.login_service.service;

import com.syspa.login_service.exceptions.InvalidEmailException;
import com.syspa.login_service.exceptions.InvalidPasswordException;
import com.syspa.login_service.exceptions.InvalidUsernameException;
import com.syspa.login_service.exceptions.NonexistentUserException;
import com.syspa.login_service.model.UserDto;
import com.syspa.login_service.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final com.syspa.login_service.utils.JwtUtil jwtUtil;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<UserDto> user = repository.findByUsername(username);
    if (user.isPresent()) {
      var userObj = user.get();
      GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userObj.getRole().name());
      return User.builder()
          .username(userObj.getUsername())
          .password(userObj.getPassword())
          .authorities(authority)
          .build();
    } else {
      throw new UsernameNotFoundException("User not found");
    }
  }

  public UserDto save(UserDto user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return repository.save(user);
  }

  public String generateToken(UserDto user) {
    return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
  }

  // --- VALIDATIONS ---

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
    if (repository.existsByEmail(email)) {
      throw new InvalidEmailException("The email is already in use");
    }
  }

  private void availableUsername(String username) {
    if (repository.existsByUsername(username)) {
      throw new InvalidUsernameException("The username is already in use");
    }
  }

  // --- BLOQUEO TEMPORAL ---
  private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
  private final Map<String, Long> blockedUntil = new ConcurrentHashMap<>();
  private static final int MAX_ATTEMPTS = 5;
  private static final long BLOCK_DURATION_MS = 5 * 60 * 1000; // 5 minutos

  public boolean isBlocked(String username) {
    Long until = blockedUntil.get(username);
    return until != null && until > System.currentTimeMillis();
  }

  public void registerFailedAttempt(String username) {
    int attempts = failedAttempts.getOrDefault(username, 0) + 1;
    failedAttempts.put(username, attempts);
    if (attempts >= MAX_ATTEMPTS) {
      blockedUntil.put(username, System.currentTimeMillis() + BLOCK_DURATION_MS);
      failedAttempts.put(username, 0);
    }
  }

  public void resetAttempts(String username) {
    failedAttempts.remove(username);
    blockedUntil.remove(username);
  }

  public void validateUser(UserDto user) {
    if (isBlocked(user.getUsername())) {
      throw new NonexistentUserException(
          "Account temporarily blocked due to multiple failed login attempts");
    }
    Optional<UserDto> foundUser = repository.findByUsername(user.getUsername());
    if (foundUser.isEmpty()) {
      registerFailedAttempt(user.getUsername());
      throw new NonexistentUserException("Invalid username or password");
    }
    boolean passwordMatches =
        passwordEncoder.matches(user.getPassword(), foundUser.get().getPassword());
    if (!passwordMatches) {
      registerFailedAttempt(user.getUsername());
      throw new NonexistentUserException("Invalid username or password");
    }
    resetAttempts(user.getUsername());
  }

  public UserDto getByUsername(String username) {
    return repository
        .findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
  }

  public void validateInput(UserDto user) {
    validateUsername(user.getUsername());
    validateEmail(user.getEmail());
    validatePassword(user.getPassword());
    availableUsername(user.getUsername());
    availableEmail(user.getEmail());
  }
}
