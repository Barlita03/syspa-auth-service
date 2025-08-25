package com.syspa.login_service.exceptions;

public class NonexistentUserException extends RuntimeException {
  public NonexistentUserException(String message) {
    super(message);
  }
}
