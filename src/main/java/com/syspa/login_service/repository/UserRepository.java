package com.syspa.login_service.repository;

import java.util.Optional;

import com.syspa.login_service.model.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserDto, Long> {
  Optional<UserDto> findByUsername(String username);
  Optional<UserDto> findByEmail(String email);
}
