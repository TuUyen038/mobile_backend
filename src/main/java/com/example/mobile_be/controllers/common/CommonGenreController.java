package com.example.mobile_be.controllers.common;

    

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mobile_be.models.Genre;
import com.example.mobile_be.repository.GenreRepository;
import com.example.mobile_be.service.ImageStorageService;

@RestController
@RequestMapping("/api/common/genres")
public class CommonGenreController {
    @Autowired
    private GenreRepository genreRepository;
    @Autowired

   
    // lay danh sach genre
    @GetMapping
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    //lay 1 genre theo id
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

   
}

