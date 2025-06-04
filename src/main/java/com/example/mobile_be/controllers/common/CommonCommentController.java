package com.example.mobile_be.controllers.common;

import com.example.mobile_be.models.Comment;
import com.example.mobile_be.repository.CommentRepository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/common/comment")

// Comment se duoc tao moi, xoa chu khong co sua

public class CommonCommentController {
    @Autowired
    private CommentRepository commentRepository;

    // [POST] http://localhost:8081/api/common/comment/create
    // Tạo comment
    @PostMapping("/create")
    public Comment postUser(@RequestBody Comment comment) {
        return commentRepository.save(comment);
    }

    // [GET] http://localhost:8081/api/common/comment
    // Lấy tất cả comment của song đó
    @GetMapping
    public List<Comment> getAllComment() {
        return commentRepository.findAll();
    }

    // [DELETE] http://localhost:8081/api/common/comment/delete/{id}
    // Xóa comment
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        ObjectId objectId = new ObjectId(id);

        if (commentRepository.existsById(objectId)) {
            commentRepository.deleteById(objectId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
