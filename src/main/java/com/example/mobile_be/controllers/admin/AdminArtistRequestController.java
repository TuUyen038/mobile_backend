// package com.example.mobile_be.controllers.admin;

// import java.util.Optional;

// import org.bson.types.ObjectId;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.example.mobile_be.models.ArtistRequest;
// import com.example.mobile_be.models.User;
// import com.example.mobile_be.repository.ArtistRequestRepository;
// import com.example.mobile_be.repository.UserRepository;

// import lombok.RequiredArgsConstructor;

// @RequiredArgsConstructor
// @RequestMapping("/api/artist-request")
// @RestController

// public class AdminArtistRequestController {
//     private final UserRepository userRepository;
//     private final ArtistRequestRepository artistRequestRepository;

//     // admin approve artist
// //     @PutMapping("/{requestId}")
// //     public ResponseEntity<?> changeArtistRequest(@PathVariable("requestId") String requestId,
// //             @RequestParam("status") String status) {
// //         if (requestId == null || requestId.length() != 24) {
// //             return ResponseEntity.badRequest().body("Invalid or missing requestId");
// //         }

// //         Optional<ArtistRequest> data = artistRequestRepository.findById(new ObjectId(requestId));
// //         if (data.isEmpty()) {
// //             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artist's request not found");
// //         }
// //         ArtistRequest art = data.get();
// //         Optional<User> user = userRepository.findById(new ObjectId(art.getUserId()));
// //         if (user.isEmpty()) {
// //             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
// //         }
// //         User artist = user.get();
// //         if ("approve".equalsIgnoreCase(status)) {
// //    //         artist.setIsVerifiedArtist(true);
// //             art.setStatus("approved");
// //         } else if ("reject".equalsIgnoreCase(status)) {
// //             art.setStatus("rejected");
// //         } else {
// //             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
// //                     .body("Invalid status provided. Use 'approve' or 'reject'.");
// //         }

// //         art.setReviewedAt(java.time.Instant.now());
// //         art.setReviewedBy("admin");

// //         userRepository.save(artist);
// //         artistRequestRepository.save(art);

// //         return ResponseEntity.ok("Request has been " + art.getStatus() + " successfully");
// //     }
// }
