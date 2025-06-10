package com.example.mobile_be.controllers.common;

import com.example.mobile_be.dto.ChangePasswordRequest;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    // [GET] http://localhost:8081/api/common/users/search?keyword=...
    // Tìm kiếm người dùng theo tên
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam("keyword") String keyword) {
        List<User> users = userRepository.findByFullNameContainingIgnoreCase(keyword);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search-artist")
    public ResponseEntity<?> searchArtistByName(@RequestParam String name) {
        List<User> artists = userService.searchVerifiedArtists(name);
        return ResponseEntity.ok(artists);
    }

    // [GET] http://localhost:8081/api/common/users/me
    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        return ResponseEntity.ok(userDetails.getUser());
    }

    // [PATCH] http://localhost:8081/api/common/users/me/change
    // người dùng tự chỉnh sửa thông tin cá nhân
    public String getLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }

    @PatchMapping("/me/change")
    public ResponseEntity<?> patchUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody User userData) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Optional<User> user = userRepository.findById(userDetails.getId());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in /me/change");
        }

        User existingUser = user.get();

        // if (userData.getEmail() != null) {
        // existingUser.setEmail(userData.getEmail());
        // }
        if (userData.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(userData.getAvatarUrl());
        }
        // if (userData.getBio() != null) {
        // existingUser.setBio(userData.getBio());
        // }
        if (userData.getFullName() != null) {
            existingUser.setFullName(userData.getFullName());
            existingUser.setLastName(getLastName(userData.getFullName()));
        }
        // if (userData.getResetToken() != null) {
        // existingUser.setResetToken(userData.getResetToken());
        // }

        User updatedUser = userRepository.save(existingUser);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/me/password/change")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody ChangePasswordRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }

}
