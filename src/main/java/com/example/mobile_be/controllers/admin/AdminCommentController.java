package com.example.mobile_be.controllers.admin;

import com.example.mobile_be.models.Comment;
import com.example.mobile_be.repository.CommentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/comment")

public class AdminCommentController {
    @Autowired
    private CommentRepository commentRepository;

    // [GET] http://localhost:8081/api/admin/comment
    // Lấy tất cả comment
    @GetMapping
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }
}
