package com.example.mobile_be.repository;

import com.example.mobile_be.models.User;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    List<User> findByFullNameContainingIgnoreCase(String keyword);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findById(ObjectId id);

    Optional<User> findByResetToken(String token);

 //   List<User> findByIsVerifiedArtistFalse();

   // List<User> findByIsVerifiedArtistTrue();


    @Aggregation(pipeline = {
            "{ $match: { role: 'ROLE_ARTIST' } }",
            "{ $lookup: { from: 'song', localField: '_id', foreignField: 'artistId', as: 'songs' } }",
            "{ $addFields: { totalViews: { $sum: '$songs.views' } } }",
            "{ $sort: { totalViews: -1 } }",
            "{ $limit: 6 }"
    })
    List<User> findTrendingArtistsWithZeroView(); 

    List<User> findByRoleAndFullNameContainingIgnoreCase(String role, String fullName);
    List<User> findByRole(String role);



}
