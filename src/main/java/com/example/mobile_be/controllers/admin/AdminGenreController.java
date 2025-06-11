package com.example.mobile_be.controllers.admin;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mobile_be.dto.GenreRequest;
import com.example.mobile_be.models.Genre;
import com.example.mobile_be.repository.GenreRepository;
import com.example.mobile_be.service.ImageStorageService;

@RestController
@RequestMapping("/api/admin/genres")
public class AdminGenreController {
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private ImageStorageService imageStorageService;

    // tao genre
    //yeu cau: content Type: multipart/form-data bởi vì có chỉnh sửa cả file ảnh

    @PostMapping()
    public ResponseEntity<?> createGenre(@ModelAttribute GenreRequest data) {
        try {
            Genre genre = new Genre();
            if (data.getName() == null || data.getName().isEmpty()) {
                throw new IllegalArgumentException("Genre name cannot be empty!");
            }
            genre.setName(data.getName());
            if (data.getDescription() != null && !data.getDescription().isEmpty()) {
                genre.setDescription(data.getDescription());
            }

            if (data.getThumbnail() != null && !data.getThumbnail().isEmpty()) {
                try {
                    String url = imageStorageService.saveFile(data.getThumbnail(), "genres");
                    genre.setThumbnailUrl(url);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to save thumbnail image", e);
                }
            }
            return ResponseEntity.ok().body(genreRepository.save(genre));
        } catch (Exception error) {
            throw new RuntimeException("Loi khi tao genre: " + error.getMessage(), error);
        }
    }

    // chinh sua genre
    //yeu cau: content Type: multipart/form-data bởi vì có chỉnh sửa cả file ảnh
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGenre(@PathVariable String id, @ModelAttribute GenreRequest data) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid genre ID");
        }

        Optional<Genre> optionalGenre = genreRepository.findById(objectId);
        if (optionalGenre.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Genre genre = optionalGenre.get();

        if (data.getName() != null && !data.getName().isEmpty()) {
            genre.setName(data.getName());
        }

        if (data.getDescription() != null && !data.getDescription().isEmpty()) {
            genre.setDescription(data.getDescription());
        }

        if (data.getThumbnail() != null && !data.getThumbnail().isEmpty()) {
            try {
                String url = imageStorageService.saveFile(data.getThumbnail(), "genres");
                genre.setThumbnailUrl(url);
            } catch (Exception e) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to save thumbnail image: " + e.getMessage());
            }
        }

        Genre updatedGenre = genreRepository.save(genre);
        return ResponseEntity.ok(updatedGenre);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGenre(@PathVariable String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        genreRepository.deleteById(objectId);
        return ResponseEntity.ok().body("Genre deleted successfully");
    }
}
