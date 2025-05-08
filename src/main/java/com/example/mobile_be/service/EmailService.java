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
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(to);
    message.setSubject(subject);
    message.setText(content);
    message.setFrom(adminEmail);

    mailSender.send(message);
    System.out.println("Email sent to " + to + " with subject: " + subject);

  }

  // noi dug trong mail gui user
  //doan nay can doi lai reset link cho dung voi frontend
  public void sendPasswordResetEmail(String to, String token) {
    String resetLink = "http://localhost:3000/reset-password?token=" + token;
    String subject = "Đặt lại mật khẩu tài khoản";
    String content = "Bạn đã yêu cầu đặt lại mật khẩu. Nhấn vào liên kết sau để thực hiện:\n"
        + resetLink + "\n\n"
        + "Liên kết có hiệu lực trong 30 phút.\n";

    sendEmail(to, subject, content);
    System.out.println("Reset link: " + resetLink);
  }
}
