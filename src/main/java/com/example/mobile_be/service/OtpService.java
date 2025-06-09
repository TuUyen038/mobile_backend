package com.example.mobile_be.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;

import com.example.mobile_be.dto.RegisterRequest;
import com.example.mobile_be.models.Otp;

@Service
public class OtpService {

    private final Map<String, Otp> otpMap = new ConcurrentReferenceHashMap<>();
    private final Map<String, PendingUser> pendingUsers = new ConcurrentHashMap<>();

    public static class PendingUser {
        private final RegisterRequest request;
        private final String otp;
        private final LocalDateTime expiryTime;

        public PendingUser(RegisterRequest request, String otp, LocalDateTime expiryTime) {
            this.request = request;
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public RegisterRequest getRequest() {
            return request;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }

    // Tạo và lưu OTP đơn giản (dùng cho login, quên mật khẩu)
    public String generateOtp(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        otpMap.put(email, new Otp(otp, LocalDateTime.now().plusMinutes(5)));
        return otp;
    }

    // Tạo và lưu OTP + RegisterRequest tạm thời (dùng cho đăng ký)
    public String generateAndStorePendingUser(RegisterRequest request) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        pendingUsers.put(request.getEmail(), new PendingUser(request, otp, expiry));
        otpMap.put(request.getEmail(), new Otp(otp, expiry));
        return otp;
    }

    // Xác minh OTP
    public boolean isValidOtp(String email, String otp) {
        Otp savedOtp = otpMap.get(email);
        if (savedOtp == null) return false;

        if (LocalDateTime.now().isAfter(savedOtp.getExpiresAt())) {
            otpMap.remove(email);
            pendingUsers.remove(email);
            return false;
        }

        boolean valid = savedOtp.getOtp().equals(otp);
        if (valid) {
            otpMap.remove(email);
        }
        return valid;
    }

    // Truy cập pendingUsers từ UserService
    public Map<String, PendingUser> getPendingUsers() {
        return pendingUsers;
    }

}
