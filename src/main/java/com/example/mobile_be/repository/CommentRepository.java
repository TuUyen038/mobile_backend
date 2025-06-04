package com.example.mobile_be.repository;

import com.example.mobile_be.models.Comment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, ObjectId> {
    List<Comment> findByUserId(String userId);

}
