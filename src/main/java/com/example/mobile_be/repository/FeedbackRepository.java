package com.example.mobile_be.repository;

import com.example.mobile_be.models.Feedback;

import java.util.List;

import org.bson.types.ObjectId;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FeedbackRepository extends MongoRepository<Feedback, ObjectId> {
    List<Feedback> findByUserId(String userId);
}
