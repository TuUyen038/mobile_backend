package com.example.mobile_be.controllers.authentication;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.mobile_be.dto.AuthResponse;
import com.example.mobile_be.dto.LoginRequest;
import com.example.mobile_be.dto.RegisterRequest;
import com.example.mobile_be.dto.VerifyRequest;
import com.example.mobile_be.models.Playlist;
import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.PlaylistRepository;
import com.example.mobile_be.repository.UserRepository;
import com.example.mobile_be.security.JwtUtil;
import com.example.mobile_be.security.UserDetailsImpl;
import com.example.mobile_be.service.UserService;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final PasswordEncoder passwordEncoder;
    @Autowired
    PlaylistRepository playlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // [POST] /api/register - Gửi OTP và lưu thông tin user tạm thời
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already used.");
        }

        if (request.getRole() == null || request.getRole().isEmpty()) {
            request.setRole("ROLE_USER");
        }

        if (request.getRole() != null && request.getRole().equals("ROLE_ADMIN")) {
            request.setRole("ROLE_USER");
        }

        userService.sendRegisterOtp(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP sent to email. Please verify to complete registration.");
        return ResponseEntity.ok(response);
    }

    // [POST] /api/verify-email - Xác thực OTP và tạo tài khoản
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyRequest request) {
        boolean success = userService.verifyEmail(request.getEmail(), request.getOtp());
        if (!success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP.");
        }
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        String token = jwtUtil.generateToken(new UserDetailsImpl(user));
        //tao playlist Favorites
        boolean exists = playlistRepository.existsByUserIdAndName(user.getId(), "Favorites");
        if (!exists) {
            try {
                Playlist playlist = new Playlist();
                playlist.setName("Favorites");
                playlist.setDescription("A list of your favorite songs");
                playlist.setUserId(user.getId());
                playlist.setIsPublic(false);
                playlist.setThumbnailUrl("/uploads/playlists/default-img.jpg");
                playlist.setType("favourites");
                playlistRepository.save(playlist);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Loi o tao playlist: " + e.getMessage());
            }
        }

        //tao playlist 
        exists = playlistRepository.existsByUserIdAndName(user.getId(), "Your Songs");
        if (!exists) {
            try {
                Playlist playlist = new Playlist();
                playlist.setName("Your Songs");
                playlist.setDescription("A list of your songs");
                playlist.setUserId(user.getId());
                playlist.setIsPublic(false);
                playlist.setThumbnailUrl("/uploads/playlists/default-img.jpg");
                playlist.setType("your_songs");
                playlistRepository.save(playlist);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Loi o tao playlist: " + e.getMessage());
            }
        }
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // [POST] /api/resend-otp - Gửi lại OTP
    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        boolean success = userService.resendOtp(email);

        if (success) {
            return ResponseEntity.ok("A new OTP has been sent to your email.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found or already verified.");
        }
    }

    // [POST] /api/login - Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.authenticate(request.getEmail(), request.getPassword());
            String token = jwtUtil.generateToken(new UserDetailsImpl(user));
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai email hoặc mật khẩu hoặc chưa xác thực.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi hệ thống.");
        }
    }
}
