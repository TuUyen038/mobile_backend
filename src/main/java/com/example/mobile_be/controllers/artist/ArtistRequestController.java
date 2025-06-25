package com.example.mobile_be.controllers.artist;

import com.example.mobile_be.models.ArtistRequest;
import com.example.mobile_be.repository.ArtistRequestRepository;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/user/artist-request")
public class ArtistRequestController {

    @Autowired
    private ArtistRequestRepository requestRepository;

    @PostMapping
    public ArtistRequest submitRequest(@RequestBody ArtistRequest request) {
        request.setId(new ObjectId());
        request.setStatus("pending");
        request.setSubmittedAt(Instant.now());
        return requestRepository.save(request);
    }

    @GetMapping("/pending")
    public List<ArtistRequest> getPendingRequests() {
        return requestRepository.findByStatus("pending");
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<ArtistRequest> reviewRequest(@PathVariable String id, @RequestBody ArtistRequest input) {
        try {
            ObjectId objId = new ObjectId(id);
            return requestRepository.findById(objId).map(req -> {
                req.setStatus(input.getStatus());
                req.setReviewedAt(Instant.now());
                req.setReviewedBy(input.getReviewedBy());
                return ResponseEntity.ok(requestRepository.save(req));
            }).orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}