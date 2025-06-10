package com.example.mobile_be.repository;

import com.example.mobile_be.models.User;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    List<User> findByFullNameContainingIgnoreCase(String keyword);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findById(ObjectId id);

    Optional<User> findByResetToken(String token);

    List<User> findByIsVerifiedArtistFalse();

    List<User> findByIsVerifiedArtistTrue();

    List<User> findByFullNameContainingIgnoreCaseAndIsVerifiedArtistTrue(String name);

}
