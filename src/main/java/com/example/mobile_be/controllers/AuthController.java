package com.example.mobile_be.controllers;

import com.example.mobile_be.dto.AuthResponse;
import com.example.mobile_be.dto.LoginRequest;
import com.example.mobile_be.dto.RegisterRequest;
import com.example.mobile_be.dto.VerifyRequest;
import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.UserRepository;
import com.example.mobile_be.security.JwtUtil;
import com.example.mobile_be.security.UserDetailsImpl;
import com.example.mobile_be.service.OtpService;
import com.example.mobile_be.service.UserService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private OtpService otpService;

    @Autowired
    private JwtUtil jwtUtil;

    AuthController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // [POST] http://localhost:8081/api/common/users/register
    // Đăng ký 
  @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
        return ResponseEntity.badRequest().body("Email already used.");
    }
    if (request.getRole() == null || request.getRole().isEmpty()) {
        request.setRole("ROLE_USER");
    }

    User saved = userService.register(request);

    return ResponseEntity.ok("User registered successfully. Please verify your email.");
}

//xac thuc xong moi tra token o day
@PostMapping("/verify-email")
public ResponseEntity<?> verifyEmail(@RequestBody VerifyRequest request) {
    if (!otpService.isValidOtp(request.getEmail(), request.getOtp())) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP.");
    }

    boolean verified = userService.verifyEmail(request.getEmail(), request.getOtp());
    if (verified) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtUtil.generateToken(new UserDetailsImpl(user));

        return ResponseEntity.ok(new AuthResponse(token)); 
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }
}


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

    // [POST] http://localhost:8081/api/common/users/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.authenticate(request.getEmail(), request.getPassword());

            String token = jwtUtil.generateToken(new UserDetailsImpl(user));

            return ResponseEntity.ok(new AuthResponse(token));

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!!");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi hệ thống");
        }
    }

}
