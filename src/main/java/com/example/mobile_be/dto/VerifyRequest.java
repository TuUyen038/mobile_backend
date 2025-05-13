package com.example.mobile_be.dto;

import lombok.Data;

@Data
public class VerifyRequest {
 private String email;
 private String otp;
}
