package com.example.mobile_be.controllers.admin;

import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.ArtistRequestRepository;
import com.example.mobile_be.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/users")

public class AdminUserController {
    private final UserRepository userRepository;
    private final ArtistRequestRepository artistRequestRepository;

    // [GET] http://localhost:8081/api/admin/users
    // Lấy tất cả người dùng
    @GetMapping()
    public List<User> getAllUsers() {
        return userRepository.findByIsVerifiedArtistFalse();
    }

    // Lay danh sach artist
    @GetMapping("/artists")
    public List<User> getAllArtist() {
        return userRepository.findByIsVerifiedArtistTrue();
    }

    // [GET] http://localhost:8081/api/admin/users/{id}
    // Tim kiem người dùng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") String id) {
        try {
            ObjectId objId = new ObjectId(id);
            Optional<User> user = userRepository.findById(objId);

            return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã có lỗi: " + e.getMessage());
        }
    }

    // [DELETE] http://localhost:8081/api/admin/users/delete/{id}
    // Xóa người dùng
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        ObjectId objectId = new ObjectId(id);

        if (userRepository.existsById(objectId)) {
            userRepository.deleteById(objectId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
