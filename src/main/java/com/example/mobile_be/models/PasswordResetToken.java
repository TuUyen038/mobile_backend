package com.example.mobile_be.models;

import lombok.Data;
import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "resetToken")
public class PasswordResetToken {
    @Id
    private ObjectId id;
    private String token;
    private String email;
    private Instant expiresAt;
    private Instant createdAt;
}
