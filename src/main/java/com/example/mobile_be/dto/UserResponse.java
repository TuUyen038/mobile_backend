package com.example.mobile_be.dto;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String fullName;
    private String role;
    private String avatarUrl;
    private Boolean isVerifiedArtist;
    private Boolean isVerified;
}
