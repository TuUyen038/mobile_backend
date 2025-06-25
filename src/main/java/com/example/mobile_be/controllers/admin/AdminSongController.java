package com.example.mobile_be.controllers.admin;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mobile_be.models.Song;
import com.example.mobile_be.repository.SongRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/song")
public class AdminSongController {
    private final SongRepository songRepository;

    // admin approve song (isPublic se tu dong chuyen sang true) 
    @PutMapping("/approve/{songId}")
    public ResponseEntity<?> removeSong(@PathVariable("songId") String songId) {

        ObjectId oId = new ObjectId(songId);
        Optional<Song> song0 = songRepository.findById(oId);
        if (song0.isEmpty()) {
            return ResponseEntity.status(404).body("Song not found!!");
        }
        Song song = song0.get();
        song.setIsApproved(true);
        song.setIsPublic(true); 
        songRepository.save(song);
        return ResponseEntity.ok("Song approved successfully!");
    }
}
