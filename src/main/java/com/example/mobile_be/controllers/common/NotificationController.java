package com.example.mobile_be.controllers.common;

import com.example.mobile_be.models.Notification;
import com.example.mobile_be.repository.NotificationRepository;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/user/{userId}")
    public List<Notification> getUserNotifications(@PathVariable String userId) {
        return notificationRepository.findByUserId(userId);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable String id) {
        try {
            ObjectId objId = new ObjectId(id);
            return notificationRepository.findById(objId).map(n -> {
                n.setRead(true);
                return ResponseEntity.ok(notificationRepository.save(n));
            }).orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public Notification sendNotification(@RequestBody Notification notification) {
        notification.setId(new ObjectId());
        notification.setRead(false);
        return notificationRepository.save(notification);
    }
}
