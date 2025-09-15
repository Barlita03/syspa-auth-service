package com.syspa.login_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @GetMapping("/hello")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> helloAdmin() {
        return ResponseEntity.ok("Hello, ADMIN!");
    }
}
