package com.example.mobile_be.repository;

import com.example.mobile_be.models.Genre;
import com.example.mobile_be.models.Library;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
@Repository

public interface LibraryRepository extends MongoRepository<Library, ObjectId> {
    Optional<Library> findById(ObjectId id);
    Library findByUserId(String id);

}
