package com.syspa.login_service.model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserDto, Long> {
  Optional<UserDto> findByUsername(String username);
  Optional<UserDto> findByEmail(String email);
}
