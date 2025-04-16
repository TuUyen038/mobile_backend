package com.example.mobile_be.controllers.admin;

import com.example.mobile_be.models.Comment;
import com.example.mobile_be.repository.CommentRepository;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/admin/comment")
@CrossOrigin(origins = "*")

//http://localhost:8081/api/admin/comment

public class AdminCommentController {
 @Autowired
  private CommentRepository feedbackRepository;

  //[GET] Lấy tất cả comment
  @GetMapping
  public List<Comment> getAllFeedbacks() {
      return feedbackRepository.findAll();
  }

 }
