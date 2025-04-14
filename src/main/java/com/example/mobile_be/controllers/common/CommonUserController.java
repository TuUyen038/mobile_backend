// package com.example.mobile_be.controllers.common;

// import com.example.mobile_be.models.User;
// import com.example.mobile_be.repository.UserRepository;

// import org.bson.types.ObjectId;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;
// import java.util.Optional;


// @RestController
// @RequestMapping("/api/users")
// @CrossOrigin(origins = "*")
// public class CommonUserController {

//  @Autowired
//   private UserRepository userRepository;

//   //[GET] Lấy tất cả người dùng
//   @GetMapping
//   public List<User> getAllUsers() {
//       return userRepository.findAll();
//   }

//   //[GET] Lấy người dùng theo ID
//   @GetMapping("/{id}")
//   public ResponseEntity<?> getUserById(@PathVariable ("id") String id) {
//    try {
//     ObjectId objId = new ObjectId(id);
//     Optional<User> user = userRepository.findById(objId);

//     return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

//    }catch (Exception e) {
//     e.printStackTrace();
//     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//         .body("Đã có lỗi: " + e.getMessage());
//    }
//   }

//   //[POST] Tạo người dùng mới
//   public User postUser(@RequestBody User user) {
//       return userRepository.save(user);
//   }


//     //[PUT] Cập nhật người dùng
//     @PutMapping("/{id}")
//     public ResponseEntity<User> updateUser(@PathVariable("id") String id, @RequestBody User user) {
//         ObjectId objectId = new ObjectId(id); 

//         if (userRepository.existsById(objectId)) {
//             user.setId(objectId);
//             return ResponseEntity.ok(userRepository.save(user));
//         } else {
//             return ResponseEntity.notFound().build();
//         }
//     }

//     //[DELETE] Xóa người dùng
//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deleteUser(@PathVariable String id) {
//         ObjectId objectId = new ObjectId(id); 

//         if (userRepository.existsById(objectId)) {
//             userRepository.deleteById(objectId);
//             return ResponseEntity.noContent().build();
//         } else {
//             return ResponseEntity.notFound().build();
//         }
//     }

//  }
