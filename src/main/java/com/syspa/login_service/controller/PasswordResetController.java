package com.syspa.login_service.controller;

import com.syspa.login_service.model.PasswordResetToken;
import com.syspa.login_service.service.MailService;
import com.syspa.login_service.service.PasswordResetService;
import com.syspa.login_service.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/auth/V1/")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;
    private final MailService mailService;
    private final UserService userService;
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_LOGGER");

    @PostMapping("forgot-password")
    public ResponseEntity<?> requestReset(@RequestBody ForgotPasswordRequest request) {
        var userOpt = userService.getByUsername(request.getUsername());
        if (userOpt == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        PasswordResetToken token = passwordResetService.createToken(request.getUsername());
        String email = userOpt.getEmail();
    String subject = "Password Recovery";
    String text = "Use this token to reset your password:\n" + token.getToken() + "\nValid for 15 minutes.";
        mailService.sendRecoveryEmail(email, subject, text);
        auditLogger.info("event=PASSWORD_RESET_REQUEST, username={}, token={}", request.getUsername(), token.getToken());
        return ResponseEntity.ok("Recovery email sent");
    }

    @PostMapping("reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        if (!success) {
            auditLogger.warn("event=PASSWORD_RESET_FAILED, token={}", request.getToken());
            return new ResponseEntity<>("Invalid or expired token", HttpStatus.BAD_REQUEST);
        }
        auditLogger.info("event=PASSWORD_RESET_SUCCESS, token={}", request.getToken());
        return ResponseEntity.ok("Password reset successful");
    }

    public static class ForgotPasswordRequest {
        private String username;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
