package com.example.mobile_be.repository;

import com.example.mobile_be.models.Playlist;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlaylistRepository extends MongoRepository<Playlist, ObjectId> {

 
}
