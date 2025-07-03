package com.example.mobile_be.repository;

import com.example.mobile_be.models.Playlist;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlaylistRepository extends MongoRepository<Playlist, ObjectId> {
  List<Playlist> findByNameContainingIgnoreCase(String name);

  List<Playlist> findByUserId(String userId);

  boolean existsByUserIdAndName(String userId, String name);

  Optional<Playlist> findByUserIdAndName(String userId, String name);

  List<Playlist> findByUserIdAndIsPublicTrue(String userId);

  List<Playlist> findTop6ByIsPublicTrueOrderByCreatedAtDesc();

  List<Playlist> findByIsPublicTrue();
}
