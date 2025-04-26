package com.example.mobile_be.dto;

import lombok.Data;

@Data
public class RegisterRequest {
 private String email;
 private String password;
 private String name;
 private String role; //"ROLE_USER" or "ROLE_ADMIN"
}
