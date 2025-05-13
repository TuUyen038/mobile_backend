package com.example.mobile_be.repository;

import com.example.mobile_be.models.PasswordResetToken;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, ObjectId> {
    PasswordResetToken findByToken(String token);
    PasswordResetToken findByEmail(String email);
}
