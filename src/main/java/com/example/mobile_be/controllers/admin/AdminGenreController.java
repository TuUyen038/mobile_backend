package com.example.mobile_be.controllers.admin;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mobile_be.models.Genre;
import com.example.mobile_be.repository.GenreRepository;

@RestController
@RequestMapping("/api/admin/genres")
public class AdminGenreController {
    @Autowired
    private GenreRepository genreRepository;

    @PostMapping
    public Genre createGenre(@RequestBody Genre genre) {
        genre.setId(new ObjectId());
        return genreRepository.save(genre);
    }

    @GetMapping
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> getGenreById(@PathVariable String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        return genreRepository.findById(objectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Genre> updateGenre(@PathVariable String id, @RequestBody Genre updatedGenre) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        return genreRepository.findById(objectId).map(genre -> {
            genre.setName(updatedGenre.getName());
            genre.setDescription(updatedGenre.getDescription());
            return ResponseEntity.ok(genreRepository.save(genre));
        }).orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        genreRepository.deleteById(objectId);
        return ResponseEntity.ok().build();
    }
}
