package com.example.mobile_be.controllers.common;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/common/users")

public class CommonUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // [GET] http://localhost:8081/api/common/users/search?keyword=...
    // Tìm kiếm người dùng theo tên
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam String keyword) {
        List<User> users = userRepository.findByFullNameContainingIgnoreCase(keyword);
        return ResponseEntity.ok(users);
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

    // [GET] http://localhost:8081/api/common/users/me
    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        return ResponseEntity.ok(userDetails.getUser());
    }

    // [POST] http://localhost:8081/api/common/users/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.authenticate(request.getEmail(), request.getPassword());
        String token = jwtUtil.generateToken(new UserDetailsImpl(user));
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // [PATCH] http://localhost:8081/api/common/users/me/change
    //người dùng tự chỉnh sửa thông tin cá nhân
    @PatchMapping("/me/change")
    public ResponseEntity<?> patchUser(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody User userData) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        
        Optional<User> user = userRepository.findById(userDetails.getId());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in /me/change");
        }
    
        User existingUser = user.get();
            
            if (userData.getEmail() != null) {
                existingUser.setEmail(userData.getEmail());
            }
            if (userData.getAvatarUrl() != null) {
                existingUser.setAvatarUrl(userData.getAvatarUrl());
            }
            if (userData.getBio() != null) {
                existingUser.setBio(userData.getBio());
            }
            if (userData.getFirstName() != null) {
                existingUser.setFirstName(userData.getFirstName());
            }
            if (userData.getLastName() != null) {
                existingUser.setLastName(userData.getLastName());
            }
            if (userData.getResetToken() != null) {
                existingUser.setResetToken(userData.getResetToken());
            }

            User updatedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(updatedUser);}

}
