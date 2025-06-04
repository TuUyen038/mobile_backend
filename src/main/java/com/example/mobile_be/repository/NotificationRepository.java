package com.example.mobile_be.repository;

import com.example.mobile_be.models.Notification;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, ObjectId> {
    List<Notification> findByUserId(String userId);

    List<Notification> findByUserIdAndIsReadFalse(String userId);
}
