package com.example.mobile_be.repository;

import com.example.mobile_be.models.Playlist;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlaylistRepository extends MongoRepository<Playlist, ObjectId> {
  List<Playlist> findByNameContainingIgnoreCase(String name);

  List<Playlist> findByUserId(String user_id);

}
