package com.example.mobile_be.controllers.artist;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.mobile_be.service.SongService;
import com.mpatric.mp3agic.Mp3File;
import com.example.mobile_be.dto.SongRequest;
import com.example.mobile_be.models.Song;
import com.example.mobile_be.repository.SongRepository;
import com.example.mobile_be.repository.UserRepository;
import com.example.mobile_be.security.UserDetailsImpl;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PathVariable;

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

  // add song
  @PostMapping("/add")
  public ResponseEntity<?> addSong(
    @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestPart("file") MultipartFile file,
      @RequestPart("title") String title,
      @RequestPart("description") String description,
      @RequestPart("coverImageUrl") String coverImageUrl,
      @RequestPart("genreId") List<String> genreId
      ) {
    try {
      Song song = new Song();
      String artistId = userDetails.getId().toString(); 
        song.setArtist_id(artistId);
      if (coverImageUrl != null && coverImageUrl.trim().length() != 0) {
        song.setCoverImageUrl(coverImageUrl);
      }
      if (title != null && title.trim().length() != 0) {
        song.setTitle(title);
      }
      if (description != null && description.trim().length() != 0) {
        song.setDescription(description);
      }
    if (genreId != null) {
      for (String genre : genreId) {
        if (!song.getGenreId().contains(genre)) {
          song.getGenreId().add(genre);
        }
      }
    }
    System.out.println("role la:   " + userDetails.getRole());

    if(userDetails.getRole().equals("ROLE_ADMIN")) {
      song.setIsPublic(true);
      song.setIsApproved(true);
    } else {
      song.setIsPublic(false);
      song.setIsApproved(false);
    }
    

      File tempMp3 = new File(System.getProperty("java.io.tmpdir"), "upload_" + System.currentTimeMillis() + ".mp3");
      Files.write(tempMp3.toPath(), file.getBytes());
      log.debug("Tạo file mp3 mới tại: {}", tempMp3.getAbsolutePath());

      try {
          Mp3File mp3File = new Mp3File(tempMp3);
          double dura = mp3File.getLengthInSeconds();
          song.setDuration(dura);
      } catch (Exception e) {
          log.error("Lỗi đọc file mp3: {}", e.getMessage(), e);
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File không hợp lệ hoặc không thể đọc được: " + e.getMessage());
      }

      songService.saveSongFile(song, file);
      return ResponseEntity.ok("Song added successfully.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Upload file failed: " + e.getMessage());
    }
  }

  // edit song
  @PutMapping("/edit/{id}")
  public ResponseEntity<?> editSong(@PathVariable("id") String id, @RequestBody SongRequest request) {
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
