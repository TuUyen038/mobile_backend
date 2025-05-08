package com.example.mobile_be.controllers;

import com.example.mobile_be.dto.ForgotPasswordRequest;
import com.example.mobile_be.models.PasswordResetToken;
import com.example.mobile_be.repository.PasswordResetTokenRepository;
import com.example.mobile_be.service.UserService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
public class PasswordResetController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @PostMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        boolean success = userService.requestPasswordReset(request.getEmail());
        if (success) {
            return ResponseEntity.ok("A password reset link has been sent to your email.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestParam(name = "token") String token,
            @RequestParam(name = "newPassword") String newPassword) {
        boolean success = userService.resetPassword(token, newPassword);
        if (success) {
            return ResponseEntity.ok("Your password has been reset successfully.");

        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
        }
    }

    @GetMapping("/test-insert")
    public String testInsert() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("test-token");
        token.setEmail("test@example.com");
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        passwordResetTokenRepository.save(token);
        return "saved";
    }

}
