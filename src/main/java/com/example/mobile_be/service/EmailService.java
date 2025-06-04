package com.example.mobile_be.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  @Autowired
  private JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String adminEmail;

  // format email
  public void sendEmail(String to, String subject, String content) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setTo(to);
      message.setSubject(subject);
      message.setText(content);
      message.setFrom(adminEmail);
      mailSender.send(message);
      System.out.println("Email sent to " + to + " with subject: " + subject);
    } catch (Exception e) {
      System.err.println("Failed to send email: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // noi dug trong mail gui user
  // doan nay can doi lai reset link cho dung voi frontend
  public void sendPasswordResetOTP(String to, String otp) {
    String subject = "Mã OTP đặt lại mật khẩu tài khoản";
    String content = "Bạn đã yêu cầu đặt lại mật khẩu.\n"
        + "Mã OTP của bạn là: " + otp + "\n\n"
        + "Mã OTP có hiệu lực trong 30 phút.\n"
        + "Nếu bạn không yêu cầu điều này, hãy bỏ qua email này.";

    sendEmail(to, subject, content);
    System.out.println("OTP: " + otp);
  }

}
