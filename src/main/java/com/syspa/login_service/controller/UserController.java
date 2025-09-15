package com.syspa.login_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @GetMapping("/hello")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> helloUser() {
        return ResponseEntity.ok("Hello, USER!");
    }
}
