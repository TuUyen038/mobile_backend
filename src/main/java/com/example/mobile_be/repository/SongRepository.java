package com.example.mobile_be.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.mobile_be.models.Song;
@Transactional
@EnableMongoRepositories
@Repository
public interface SongRepository extends MongoRepository<Song, ObjectId> {
    Optional<Song> findById(ObjectId id);

    List<Song> findByTitleContainingIgnoreCase(String title);
    List<Song> findByArtistIdIn(List<String> artistIds);
    List<Song> findByIdIn(List<ObjectId> id);

    // public List<Song> findTop10ByOrderByLastPlayedAtDesc();

    List<Song> findTop10ByOrderByCreatedAtDesc();

    List<Song> findByOrderByViewsDesc();
    
    List<Song> findAllByOrderByCreatedAtDesc();
    List<Song> findAllByOrderByCreatedAtAsc();

    List<Song> findByGenreId(String genreId);


}
