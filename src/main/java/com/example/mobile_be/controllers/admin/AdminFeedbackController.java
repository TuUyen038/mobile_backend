package com.example.mobile_be.controllers.admin;

import com.example.mobile_be.repository.FeedbackRepository;
import com.example.mobile_be.repository.UserRepository;
import com.example.mobile_be.security.UserDetailsImpl;
import com.example.mobile_be.models.Feedback;
import com.example.mobile_be.models.User;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admin/feedbacks")
public class AdminFeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    // // ✅ Admin xem feedback của một user cụ thể
    // @GetMapping("/user/{userId}")
    // public ResponseEntity<List<Feedback>> getUserFeedbacks(@PathVariable String userId) {
    //     try {
    //         List<Feedback> feedbackList = feedbackRepository.findByUserId(userId);
    //         return ResponseEntity.ok(feedbackList);
    //     } catch (IllegalArgumentException e) {
    //         return ResponseEntity.badRequest().build();
    //     }
    // }

    // ✅ Admin xem toàn bộ feedback
    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedbacks() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        return ResponseEntity.ok(feedbacks);
    }

    // ✅ Admin phản hồi một feedback
    @PutMapping("/{id}/reply")
    public ResponseEntity<Feedback> replyToFeedback(@PathVariable String id, @RequestBody Feedback input) {
        try {
            ObjectId feedbackId = new ObjectId(id);
            return feedbackRepository.findById(feedbackId).map(fb -> {
                fb.setReply(input.getReply());
                return ResponseEntity.ok(feedbackRepository.save(fb));
            }).orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable String id) {
        try {
            feedbackRepository.deleteById(new ObjectId(id));
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
