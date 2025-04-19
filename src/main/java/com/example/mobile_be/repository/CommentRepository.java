package com.example.mobile_be.repository;

import com.example.mobile_be.models.Comment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface CommentRepository extends MongoRepository<Comment, ObjectId> {
    // List<Feedback> findByUserId(String userId);
 
}
