package com.example.mobile_be.dto;

import lombok.Data;

@Data
public class ResetPasswordRequest {
 private String email;
 private String otp;
 private String newPassword;
}
