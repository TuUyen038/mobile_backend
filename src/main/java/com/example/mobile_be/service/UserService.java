package com.example.mobile_be.service;

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

  public User register(RegisterRequest request) {
    User user = new User();
    user.setEmail(request.getEmail());
    user.setLastName(getLastName(request.getFullName()));
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(request.getRole());
    user.setFullName(request.getFullName());
    user.setIsVerifiedArtist(false);
    user.setIsVerified(false);
    userRepository.save(user);

    String otp = otpService.generateOtp(request.getEmail());

    String subject = "Mã OTP xác thực tài khoản";
    String content = "\nMã OTP của bạn là: " + otp + "\nMã OTP có hiệu lực trong 5 phút.";
    emailService.sendEmail(user.getEmail(), subject, content);

    return user;

  }

  public User authenticate(String email, String rawPassword) {

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found in login"));
    if (user.getIsVerified() == false) {
      throw new BadCredentialsException("Unverified");
    }
    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
      throw new BadCredentialsException("Wrong password");
    }

    return user;
  }

  // gui mail reset password cho user
  public boolean requestPasswordReset(String email) {
    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null) {
      return false;
    }
    String otp = otpService.generateOtp(email);
    emailService.sendPasswordResetOTP(email, otp);
    return true;
  }

  // dat lai mat khau
  public boolean resetPasswordWithOtp(ResetPasswordRequest request) {
    boolean isValid = otpService.isValidOtp(request.getEmail(), request.getOtp());
    if (!isValid) {
      System.out.println("OTP không hợp lệ hoặc đã hết hạn");
      return false;
    }

    User user = userRepository.findByEmail(request.getEmail()).orElse(null);
    if (user == null)
      return false;

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));

    // verifyOtp, generateOtp, ConcurrentHashMap

    userRepository.save(user);

    System.out.println("Đã lưu mật khẩu mới cho user: " + user.getEmail());
    return true;

  }

  public boolean verifyEmail(String email, String otp) {
    User user = userRepository.findByEmail(email).orElse(null);

    if (user == null) {
      return false;
    }
    user.setIsVerified(true);
    userRepository.save(user);
    return true;
  }

  public boolean resendOtp(String email) {
    User user = userRepository.findByEmail(email).orElse(null);

    if (user == null || user.getIsVerified())
      return false;

    String newOtp = otpService.generateOtp(email);
    String subject = "Mã OTP xác thực tài khoản";
    String content = "\nMã OTP mới của bạn là: " + newOtp + "\nMã OTP có hiệu lực trong 5 phút.";

    emailService.sendEmail(email, subject, content);
    return true;
  }

}
