package com.example.mobile_be.controllers.common;

import com.example.mobile_be.models.Comment;
import com.example.mobile_be.repository.CommentRepository;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/comment")
@CrossOrigin(origins = "*")

//http://localhost:8081/api/comment/...

//Comment se duoc tao moi, xoa chu khong co sua

public class CommonCommentController {
 @Autowired
  private CommentRepository feedbackRepository;

  //[POST] Tạo comment 
  @PostMapping("/create")
  public Comment postUser(@RequestBody Comment feedback) {
      return feedbackRepository.save(feedback);
  }

    //[DELETE] Xóa comment
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        ObjectId objectId = new ObjectId(id); 

        if (feedbackRepository.existsById(objectId)) {
            feedbackRepository.deleteById(objectId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

 }
