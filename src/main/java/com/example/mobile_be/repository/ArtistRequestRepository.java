package com.example.mobile_be.repository;

import com.example.mobile_be.models.ArtistRequest;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArtistRequestRepository extends MongoRepository<ArtistRequest, ObjectId> {
    List<ArtistRequest> findByStatus(String status);

    List<ArtistRequest> findByUserId(ObjectId user_id);
}
