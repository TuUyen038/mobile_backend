package com.example.mobile_be.controllers.admin;


import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.UserRepository;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;



@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*")

//http://localhost:8081/api/admin/users/...

public class AdminUserController {

 @Autowired
  private UserRepository userRepository;

  //[GET] Lấy tất cả người dùng
  @GetMapping
  public List<User> getAllUsers() {
    System.out.println("Thông điệp log của bạn");

      return userRepository.findAll();
  }

  //[GET] Lấy người dùng theo ID
  @GetMapping("/{id}")
  public ResponseEntity<?> getUserById(@PathVariable ("id") String id) {
   try {
    ObjectId objId = new ObjectId(id);
    Optional<User> user = userRepository.findById(objId);

    return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

   }catch (Exception e) {
    e.printStackTrace();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Đã có lỗi: " + e.getMessage());
   }
  }

  //[POST] Tạo 1 người dùng mới
  @PostMapping("/create")
  public User postUser(@RequestBody User user) {
      return userRepository.save(user);
  }


    //[PUT] Cập nhật người dùng
    // @PutMapping("/change/{id}")
    // public ResponseEntity<User> updateUser(@PathVariable("id") String id, @RequestBody User userData) {
    //     if (!ObjectId.isValid(id)) {
    //         return ResponseEntity.badRequest().build();
    //     }
     @PutMapping("/change/{id}")
public ResponseEntity<User> patchUser(@PathVariable("id") String id, @RequestBody User userData) {
    if (!ObjectId.isValid(id)) {
        return ResponseEntity.badRequest().build();
    }
    ObjectId objectId = new ObjectId(id);
    Optional<User> optionalUser = userRepository.findById(objectId);

    if (optionalUser.isPresent()) {
        User existingUser = optionalUser.get();

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

        return ResponseEntity.ok(userRepository.save(existingUser));
    } else {
        return ResponseEntity.notFound().build();
    }
}

    //[DELETE] Xóa người dùng
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
