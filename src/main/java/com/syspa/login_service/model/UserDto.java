package com.syspa.login_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

@Getter
@Setter
@Entity
@Table(name = "users_auth")
public class UserDto {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
  private UUID id;


  @NotBlank(message = "Username is required")
  @Size(min = 5, message = "Username must be at least 5 characters long")
  @Column(nullable = false)
  private String username;


  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  @Column(nullable = false)
  private String password;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;
  private String role = "USER";
}
