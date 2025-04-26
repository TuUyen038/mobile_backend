package com.example.mobile_be.controllers.admin;

import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.UserRepository;
import com.example.mobile_be.security.UserDetailsImpl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")

public class AdminUserController {

    @Autowired
    private UserRepository userRepository;

    // [GET] http://localhost:8081/api/admin/users
    // Lấy tất cả người dùng
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // [GET] http://localhost:8081/api/admin/users/{id}
    // Lấy người dùng theo ID
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

    // [GET] http://localhost:8081/api/admin/users/search?keyword=...
    // Tìm kiếm người dùng theo tên
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByName(@RequestParam String keyword) {
        List<User> users = userRepository.findByNameContainingIgnoreCase(keyword);
        return ResponseEntity.ok(users);
    }

     // [PATCH] http://localhost:8081/api/admin/users/change
    // Cập nhật người dùng
    @PatchMapping("/change/{id}")
    public ResponseEntity<?> patchUser(@PathVariable("id") String id, @RequestBody User userData) {
        
        Optional<User> user = userRepository.findById(new ObjectId(id));
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found in /admin/users/change");
        }
    
        User existingUser = user.get();
            if (userData.getName() != null) {

                existingUser.setName(userData.getName());
            }
            if (userData.getEmail() != null) {
                existingUser.setEmail(userData.getEmail());
            }

            if (userData.getRole() != null) {
                existingUser.setRole(userData.getRole());
            }
            if (userData.getAvatar_url() != null) {
                existingUser.setAvatar_url(userData.getAvatar_url());
            }
            if (userData.getFavorite_song() != null) {
                existingUser.setFavorite_song(userData.getFavorite_song());
            }

            User updatedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(updatedUser);}



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
