package com.example.mobile_be.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;

import com.example.mobile_be.models.Otp;

@Service
public class OtpService {
    private final Map<String, Otp> otpMap = new ConcurrentReferenceHashMap<>();

    public String generateOtp(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        otpMap.put(email, new Otp(otp, LocalDateTime.now().plusMinutes(5)));
        return otp;
    }

    public boolean isValidOtp(String email, String otp) {
        Otp otp2 = otpMap.get(email);
        if (otp2 == null)
            return false;

        if (LocalDateTime.now().isAfter(otp2.getExpiresAt())) {
            otpMap.remove(email);
            return false;
        }
        boolean valid = otp2.getOtp().equals(otp);
        if (valid)
            otpMap.remove(email); // xoa sau khi dung
        return valid;
    }

}