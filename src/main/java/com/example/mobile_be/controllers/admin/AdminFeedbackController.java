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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedbacks")
public class AdminFeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    // Lấy user hiện tại đã đăng nhập
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    // Người dùng gửi feedback
    @PostMapping
    public ResponseEntity<Feedback> sendFeedback(@RequestBody Feedback feedback) {
        User currentUser = getCurrentUser();
        feedback.setId(new ObjectId());
        feedback.setUserId(currentUser.getId());
        feedback.setStatus("Pending");
        return ResponseEntity.ok(feedbackRepository.save(feedback));
    }

    // ✅ Người dùng xem các feedback của chính họ
    @GetMapping("/me")
    public ResponseEntity<List<Feedback>> getMyFeedbacks() {
        User currentUser = getCurrentUser();
        List<Feedback> feedbacks = feedbackRepository.findByUserId(currentUser.getId());
        return ResponseEntity.ok(feedbacks);
    }

    // ✅ Admin xem feedback của một user cụ thể
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Feedback>> getUserFeedbacks(@PathVariable String userId) {
        try {
            List<Feedback> feedbackList = feedbackRepository.findByUserId(userId);
            return ResponseEntity.ok(feedbackList);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ Admin xem toàn bộ feedback (tùy chọn lọc theo status)
    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedbacks(@RequestParam(required = false) String status) {
        List<Feedback> feedbacks;
        if (status != null && !status.isEmpty()) {
            feedbacks = feedbackRepository.findByStatus(status);
        } else {
            feedbacks = feedbackRepository.findAll();
        }
        return ResponseEntity.ok(feedbacks);
    }

    // ✅ Admin phản hồi một feedback
    @PutMapping("/{id}/reply")
    public ResponseEntity<Feedback> replyToFeedback(@PathVariable String id, @RequestBody Feedback input) {
        try {
            ObjectId feedbackId = new ObjectId(id);
            return feedbackRepository.findById(feedbackId).map(fb -> {
                fb.setAdminReply(input.getAdminReply());
                fb.setStatus("reviewed");
                return ResponseEntity.ok(feedbackRepository.save(fb));
            }).orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
