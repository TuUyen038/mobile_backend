package com.example.mobile_be.controllers.artist;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.mobile_be.dto.SongRequest;
import com.example.mobile_be.models.Song;
import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.SongRepository;
import com.example.mobile_be.repository.UserRepository;
import com.example.mobile_be.security.UserDetailsImpl;
import com.example.mobile_be.service.SongService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/artist/song")
public class SongController {
   private final SongService songService;
   private final SongRepository songRepository;
   private final UserRepository userRepository;

   public SongController(SongService s, SongRepository r, UserRepository u) {
      songService = s;
      songRepository = r;
      userRepository = u;
   }

   private User getCurrentUser() {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
      return userRepository.findById(userDetails.getId())
            .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
   }

   // add song
   @PostMapping("/add")
   public ResponseEntity<?> addSong(@RequestPart("file") MultipartFile file, @RequestPart("title") String title,
         @RequestPart("description") String description,
         @RequestPart("coverImageUrl") String coverImageUrl) {
      User user = getCurrentUser();

      try {
         Song song = new Song();
         song.setArtistId(user.getId());
         if (coverImageUrl != null && !coverImageUrl.trim().isEmpty()) {
            song.setCoverImageUrl(coverImageUrl);
         }
         if (title != null && !title.trim().isEmpty()) {
            song.setTitle(title);
         }
         if (description != null && !description.trim().isEmpty()) {
            song.setDescription(description);
         }
         song.setIsPublic(false);
         songService.saveSongFile(song, file);
         return ResponseEntity.ok("Song added successfully.");
      } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
               .body("Upload file failed: " + e.getMessage());
      }
   }

   // stream file .mp3
   @GetMapping("/stream/{id}")
   public void streamSong(@PathVariable ObjectId id, HttpServletResponse res) {
      Optional<Song> test = songService.getSongById(id);
      if (test.isEmpty()) {
         res.setStatus(HttpServletResponse.SC_NOT_FOUND);
         try {
            res.getWriter().write("Song not found!!");
         } catch (java.io.IOException e) {
            // ignore
         }
         return;
      }
      Song song = test.get();
      File songFile = new File(song.getAudioUrl());
      if (!songFile.exists()) {
         res.setStatus(HttpServletResponse.SC_NOT_FOUND);
         try {
            res.getWriter().write("File not found!!");
         } catch (java.io.IOException e) {
            // ignore
         }
         return;
      }
      res.setContentType("audio/mpeg");
      res.setHeader("Content-Disposition", "inline; filename=\"" + songFile.getName() + "\"");
      try (InputStream iStream = new FileInputStream(songFile);
           OutputStream oStream = res.getOutputStream()) {
         byte[] buffer = new byte[4096];
         int bytesRead;
         while ((bytesRead = iStream.read(buffer)) != -1) {
            oStream.write(buffer, 0, bytesRead);
         }
         oStream.flush();
      } catch (java.io.IOException e) {
         res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
         try {
            res.getWriter().write("Error streaming file: " + e.getMessage());
         } catch (java.io.IOException ex) {
            // ignore
         }
      }
   }

   // edit song
   @PutMapping("/edit/{id}")
   public ResponseEntity<?> editSong(@PathVariable("id") String id, @RequestBody SongRequest request) {
      getCurrentUser();

      ObjectId oId = new ObjectId(id);
      Optional<Song> song0 = songRepository.findById(oId);
      if (song0.isEmpty()) {
         return ResponseEntity.status(404).body("Song not found!!");
      }

      Song song = song0.get();
      try {
         if (request.getCoverImageUrl() != null) {
            song.setCoverImageUrl(request.getCoverImageUrl());
         }
         if (request.getTitle() != null) {
            song.setTitle(request.getTitle());
         }
         if (request.getDescription() != null) {
            song.setDescription(request.getDescription());
         }
         songRepository.save(song);
         return ResponseEntity.ok(song);
      } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
               .body("Edit song failed: " + e.getMessage());
      }
   }
}
