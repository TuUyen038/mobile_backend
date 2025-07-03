package com.example.mobile_be.controllers.authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
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
import com.example.mobile_be.models.Library;
import com.example.mobile_be.models.Playlist;
import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.LibraryRepository;
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
    @Autowired
    private LibraryRepository libraryRepository;

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

        // Chuẩn bị danh sách playlist mặc định
        List<String> defaultPlaylistIds = new ArrayList<>();

        // Favorites
        if (!playlistRepository.existsByUserIdAndName(user.getId(), "Favorites")) {
            try {
                Playlist favorites = new Playlist();
                favorites.setName("Favorites");
                favorites.setDescription("A list of your favorite songs");
                favorites.setUserId(user.getId());
                favorites.setIsPublic(false);
                favorites.setThumbnailUrl("/uploads/playlists/default-img.jpg");
                favorites.setPlaylistType("favourites");

                playlistRepository.save(favorites);
                defaultPlaylistIds.add(favorites.getId()); // Lưu ID vào danh sách
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Lỗi khi tạo playlist Favorites: " + e.getMessage());
            }
        }

        // Your Songs
        if (!playlistRepository.existsByUserIdAndName(user.getId(), "Your Songs")) {
            try {
                Playlist yourSongs = new Playlist();
                yourSongs.setName("Your Songs");
                yourSongs.setDescription("A list of your songs");
                yourSongs.setUserId(user.getId());
                yourSongs.setIsPublic(false);
                yourSongs.setThumbnailUrl("/uploads/playlists/default-img.jpg");
                yourSongs.setPlaylistType("your_songs");

                playlistRepository.save(yourSongs);
                defaultPlaylistIds.add(yourSongs.getId());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Lỗi khi tạo playlist Your Songs: " + e.getMessage());
            }
        }

        Library library = libraryRepository.findByUserId(user.getId());
        if (library == null) {
            library = new Library();
            library.setUserId(user.getId());
            library.setPlaylistIds(new ArrayList<>());
        }

        for (String pid : defaultPlaylistIds) {
            if (!library.getPlaylistIds().contains(pid)) {
                library.getPlaylistIds().add(pid);
            }
        }

        libraryRepository.save(library);
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
