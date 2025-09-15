package com.syspa.login_service.repository;


import com.syspa.login_service.model.UserDto;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface UserRepository extends JpaRepository<UserDto, UUID> {
  Optional<UserDto> findByUsername(String username);

  Optional<UserDto> findByEmail(String email);
}
