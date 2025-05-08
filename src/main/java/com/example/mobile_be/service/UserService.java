package com.example.mobile_be.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.mobile_be.dto.RegisterRequest;
import com.example.mobile_be.models.PasswordResetToken;
import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.PasswordResetTokenRepository;
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
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new UserDetailsImpl(user);
  }

  public User register(RegisterRequest request) {
    User user = new User();
    user.setEmail(request.getEmail());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setRole(request.getRole());
    user.setFullName(request.getFirstName() + " " + request.getLastName());
    user.setIsVerifiedArtist(false);
    return userRepository.save(user);
  }

  public User authenticate(String email, String rawPassword) {

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found in login"));
       
    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
      throw new BadCredentialsException("Wrong password");
    }

    return user;
  }


  //gui mail reset password cho user
  public boolean requestPasswordReset(String email) {
    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null) {
      return false; 
    }

    String token = UUID.randomUUID().toString(); 
    PasswordResetToken resetToken = new PasswordResetToken();
    resetToken.setToken(token);
    resetToken.setEmail(email);
    resetToken.setCreatedAt(Instant.now());
    resetToken.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));

    passwordResetTokenRepository.save(resetToken); 
    System.out.println("====== Da qua reset token: ");

    emailService.sendPasswordResetEmail(email, token); 
    return true;
  }


// dat lai mat khau
  public boolean resetPassword(String token, String newPassword) {
    PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
    if (resetToken == null) {
      System.out.println("====== resetToken null");
      return false; 
    }
    
    User user = userRepository.findByEmail(resetToken.getEmail()).orElse(null);

    // Kiểm tra xem token có tồn tại trong database không
    if (resetToken.getExpiresAt().isBefore(Instant.now())) {
      System.out.println("====== token da het han");
      passwordResetTokenRepository.delete(resetToken); // Xóa token hết hạn
      return false;
    }

    if (user != null) {

      user.setPassword(passwordEncoder.encode(newPassword));

      userRepository.save(user);
      passwordResetTokenRepository.delete(resetToken); // Xóa token sau khi reset

      System.out.println("Đã lưu mật khẩu mới cho user: " + user.getEmail());
System.out.println("Mật khẩu mã hóa: " + user.getPassword());

      return true;
    }
    return false;
  }
}
