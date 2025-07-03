package com.example.mobile_be.controllers.common;

import com.example.mobile_be.dto.ChangePasswordRequest;
import com.example.mobile_be.dto.UserResponse;
import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.SongRepository;
import com.example.mobile_be.repository.UserRepository;
import com.example.mobile_be.security.JwtUtil;
import com.example.mobile_be.security.UserDetailsImpl;
import com.example.mobile_be.service.ImageStorageService;
import com.example.mobile_be.service.UserService;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/common/users")

public class CommonUserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ImageStorageService imageStorageService;
    @Autowired
    SongRepository songRepository;

    @PatchMapping("/switch-role")
    public ResponseEntity<?> toggleUserRole() {
        User currentUser = getCurrentUser();

        String currentRole = currentUser.getRole();
        
        if (currentRole.equals("ROLE_USER")) {
            currentUser.setRole("ROLE_ARTIST");
        } else if (currentRole.equals("ROLE_ARTIST")) {
            currentUser.setRole("ROLE_USER");
        } else {
            return ResponseEntity.badRequest().body("Unsupported role: " + currentRole);
        }

        userRepository.save(currentUser); 
        return ResponseEntity.ok("User role updated to: " + currentUser.getRole());
    }

    @GetMapping("/trending-artists")
    public ResponseEntity<?> getTrendingArtists() {
        List<User> artistUsers = userRepository.findTrendingArtistsWithZeroView();

        List<UserResponse> responses = artistUsers.stream().map(user -> {
            UserResponse dto = new UserResponse();
            dto.setId(user.getId());
            dto.setEmail(user.getEmail());
            dto.setFullName(user.getFullName());
            dto.setRole(user.getRole());
            dto.setAvatarUrl(user.getAvatarUrl());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // [GET] http://localhost:8081/api/common/users/search?keyword=...
    // Tìm kiếm người dùng theo tên
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam("keyword") String keyword) {
        List<User> users = userRepository.findByFullNameContainingIgnoreCase(keyword);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search-artist")
    public ResponseEntity<?> searchArtistByName(@RequestParam String name) {
        List<User> artists = userRepository.findByRoleAndFullNameContainingIgnoreCase("ROLE_ARTIST", name);

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

    @PatchMapping(value = "/me/change", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> patchUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestPart(value = "fullName", required = false) String fullName,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in /me/change");
        }

        User existingUser = userOpt.get();

        if (avatar != null && !avatar.isEmpty()) {
            try {
                String url = imageStorageService.saveFile(avatar, "images");
                existingUser.setAvatarUrl(url);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to upload avatar: " + e.getMessage());
            }

        }

        if (fullName != null) {
            existingUser.setFullName(fullName);
            existingUser.setLastName(getLastName(fullName));
        }

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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

}
