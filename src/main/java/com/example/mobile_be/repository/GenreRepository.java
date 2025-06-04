package com.example.mobile_be.repository;

import com.example.mobile_be.models.Genre;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GenreRepository extends MongoRepository<Genre, ObjectId> {
    Optional<Genre> findById(ObjectId id);

    Genre findByName(String name);
}
