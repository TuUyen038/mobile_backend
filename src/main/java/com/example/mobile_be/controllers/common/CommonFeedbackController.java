package com.example.mobile_be.controllers.common;

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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common/feedbacks")
public class CommonFeedbackController {

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
        return ResponseEntity.ok(feedbackRepository.save(feedback));
    }

    // ✅ Người dùng xem các feedback của chính họ
    @GetMapping("/me")
    public ResponseEntity<List<Feedback>> getMyFeedbacks() {
        User currentUser = getCurrentUser();
        List<Feedback> feedbacks = feedbackRepository.findByUserId(currentUser.getId());
        return ResponseEntity.ok(feedbacks);
    }
}
