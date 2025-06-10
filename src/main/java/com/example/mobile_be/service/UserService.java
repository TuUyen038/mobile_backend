package com.example.mobile_be.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.mobile_be.dto.RegisterRequest;
import com.example.mobile_be.dto.ResetPasswordRequest;
import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.UserRepository;
import com.example.mobile_be.security.UserDetailsImpl;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new UserDetailsImpl(user);
    }

    public String getLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }

    // Gửi OTP và lưu tạm thông tin đăng ký
    public void sendRegisterOtp(RegisterRequest request) {
        String otp = otpService.generateAndStorePendingUser(request);

        String subject = "Mã OTP xác thực tài khoản";
        String content = "Xin chào " + request.getFullName()
                + ",\n\nMã OTP của bạn là: " + otp + "\nMã OTP có hiệu lực trong 5 phút.";

        emailService.sendEmail(request.getEmail(), subject, content);
    }

    // Xác minh OTP và tạo tài khoản thực
    public boolean verifyEmail(String email, String otp) {
        boolean isValid = otpService.isValidOtp(email, otp);
        if (!isValid)
            return false;

        OtpService.PendingUser pending = otpService.getPendingUsers().remove(email);
        if (pending == null)
            return false;

        RegisterRequest request = pending.getRequest();

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setLastName(getLastName(request.getFullName()));
        user.setRole(request.getRole());
        user.setIsVerified(true);
        user.setIsVerifiedArtist(false);

        userRepository.save(user);
        return true;
    }

    // Đăng nhập
    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in login"));
        if (!user.getIsVerified()) {
            throw new BadCredentialsException("Unverified");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Wrong password");
        }
        return user;
    }

    // Gửi mail reset password
    public boolean requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null)
            return false;

        String otp = otpService.generateOtp(email);
        emailService.sendPasswordResetOTP(email, otp);
        return true;
    }

    // Đặt lại mật khẩu bằng OTP
    public boolean resetPasswordWithOtp(ResetPasswordRequest request) {
        boolean isValid = otpService.isValidOtp(request.getEmail(), request.getOtp());
        if (!isValid)
            return false;

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null)
            return false;

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    // Gửi lại OTP đăng ký (chưa xác thực)
    public boolean resendOtp(String email) {
        OtpService.PendingUser pending = otpService.getPendingUsers().get(email);
        if (pending == null)
            return false;

        String newOtp = otpService.generateAndStorePendingUser(pending.getRequest());

        String subject = "Mã OTP mới xác thực tài khoản";
        String content = "Xin chào " + pending.getRequest().getFullName()
                + ",\n\nMã OTP mới của bạn là: " + newOtp + "\nMã OTP có hiệu lực trong 5 phút.";

        emailService.sendEmail(email, subject, content);
        return true;
    }

    public List<User> searchVerifiedArtists(String name) {
        return userRepository.findByFullNameContainingIgnoreCaseAndIsVerifiedArtistTrue(name);
    }
}
