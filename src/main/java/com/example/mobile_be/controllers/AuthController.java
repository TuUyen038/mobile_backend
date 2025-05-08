package com.example.mobile_be.controllers;

import com.example.mobile_be.dto.AuthResponse;
import com.example.mobile_be.dto.LoginRequest;
import com.example.mobile_be.dto.RegisterRequest;
import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.UserRepository;
import com.example.mobile_be.security.JwtUtil;
import com.example.mobile_be.security.UserDetailsImpl;
import com.example.mobile_be.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    AuthController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // [POST] http://localhost:8081/api/common/users/register
    // Đăng ký (user nhap email, password, firstName, lastName)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already used.");
        }
        if (request.getRole() == null || request.getRole().isEmpty()) {
            request.setRole("ROLE_USER");
        }

        User saved = userService.register(request);
        String token = jwtUtil.generateToken(new UserDetailsImpl(saved));
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // [POST] http://localhost:8081/api/common/users/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.authenticate(request.getEmail(), request.getPassword());
        String token = jwtUtil.generateToken(new UserDetailsImpl(user));
        

        return ResponseEntity.ok(new AuthResponse(token));
    }

}
