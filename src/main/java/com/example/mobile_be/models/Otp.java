package com.example.mobile_be.models;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Otp {
 private String otp;
 private LocalDateTime expiresAt;
 public Otp(String otp, LocalDateTime ex) {
  this.otp = otp;
  expiresAt = ex;
 }
}
