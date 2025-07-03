package com.example.mobile_be.controllers.common;

import com.example.mobile_be.models.Library;
import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.LibraryRepository;
import com.example.mobile_be.security.UserDetailsImpl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/common/library")
public class LibraryController {

    @Autowired
    private LibraryRepository libraryRepository;

    // getLibrary
    @GetMapping("")
    public ResponseEntity<?> getLibrary() {
        String userId = getCurrentUser().getId();
        Library library = libraryRepository.findByUserId((userId));
        return ResponseEntity.ok(library);
    }

    // addPlaylistToLibrary
    @PostMapping("/add/{playlistId}")
    public ResponseEntity<?> addPlaylistToLibrary(@PathVariable String playlistId) {
        String userId = getCurrentUser().getId();

        Library library = libraryRepository.findByUserId((userId));
        if (library == null) {
            library = new Library();
            library.setUserId(userId);
            library.setPlaylistIds(new ArrayList<>());
        }
        if (!library.getPlaylistIds().contains(playlistId)) {
            library.getPlaylistIds().add(playlistId);
            libraryRepository.save(library);
        }
        return ResponseEntity.ok("Playlist added to library.");
    }

    // removePlaylistFromLibrary
    @DeleteMapping("/remove/{playlistId}")
    public ResponseEntity<?> removePlaylistFromLibrary(@PathVariable String playlistId) {
        String userId = getCurrentUser().getId();

        Library library = libraryRepository.findByUserId((userId));
        if (library == null) {
            return ResponseEntity.status(404).body("Library not found.");
        }

        boolean removed = library.getPlaylistIds().remove(playlistId);
        if (removed) {
            libraryRepository.save(library);
            return ResponseEntity.ok("Playlist removed from library.");
        } else {
            return ResponseEntity.status(400).body("Playlist not found in library.");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getUser();
    }
}
