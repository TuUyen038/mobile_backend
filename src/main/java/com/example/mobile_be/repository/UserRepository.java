package com.example.mobile_be.repository;


import com.example.mobile_be.models.User;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    List<User> findByNameContainingIgnoreCase(String keyword);

}
