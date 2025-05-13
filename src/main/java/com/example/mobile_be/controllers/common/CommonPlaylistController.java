package com.example.mobile_be.controllers.common;

import com.example.mobile_be.dto.PlaylistRequest;
import com.example.mobile_be.models.Playlist;
import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.PlaylistRepository;
import com.example.mobile_be.repository.UserRepository;
import com.example.mobile_be.security.UserDetailsImpl;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/common/playlist")

public class CommonPlaylistController {
  @Autowired
  private PlaylistRepository playlistRepository;
  @Autowired
  UserRepository userRepository;

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    return userRepository.findById(userDetails.getId())
        .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
  }

  // [GET] http://localhost:8081/api/common/playlist
  // lấy tất cả playlist cua user
  @GetMapping
  public ResponseEntity<List<Playlist>> getAllPlaylists() {
    User user = getCurrentUser();
    List<Playlist> playlists = playlistRepository.findByUserId(user.getId());
    return ResponseEntity.ok(playlists);
  }

  // [GET] http://localhost:8081/api/common/playlist/{playlistId}
  // lấy playlist theo ID
  @GetMapping("/{id}")
  public ResponseEntity<?> getPlaylistById(@PathVariable("id") String id) {
    try {
      ObjectId objId = new ObjectId(id);
      Optional<Playlist> playlistOpt = playlistRepository.findById(objId);

      if (playlistOpt.isEmpty())
        return ResponseEntity.notFound().build();

      Playlist playlist = playlistOpt.get();
      if (!playlist.getUserId().equals(getCurrentUser().getId())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
      }

      return ResponseEntity.ok(playlist);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error: " + e.getMessage());
    }
  }

  // [GET] http://localhost:8081/api/commonplaylist/search?keyword=...
  // tìm kiếm playlist theo tên
  @GetMapping("/search")
  public ResponseEntity<List<Playlist>> searchPlaylistByName(@RequestParam String keyword) {
    User user = getCurrentUser();

    List<Playlist> playlists = playlistRepository.findByNameContainingIgnoreCase(keyword, user.getId());
    return ResponseEntity.ok(playlists);
  }

  // [POST] http://localhost:8081/api/common/playlist/create
  // tạo playlist
  @PostMapping("/create")
  public ResponseEntity<?> postPlaylist(@RequestBody PlaylistRequest request) {
    // Lấy user đang đăng nhập từ SecurityContext
    User user = getCurrentUser();

    Playlist playlist = new Playlist();
    playlist.setName(request.getName());
    playlist.setDescription(request.getDescription());
    playlist.setUserId(user.getId());
    playlist.setThumbnailUrl(request.getThumbnailUrl());
    playlist.setIsPublic(false);
    playlistRepository.save(playlist);
    return ResponseEntity.status(200).body("Playlist created successfully.");
  }

  // [PATCH] http://localhost:8081/api/common/playlist/change/{playlistId}
  // Chỉ bao gồm thay đổi name, thumbnail, description
  // neu thay doi thuoc tinh khac thi mac du 200 OK nhung thuoc tinh do van k bi
  @PatchMapping("/change/{id}")
  public ResponseEntity<?> updateUser(@PathVariable("id") String id, @RequestBody Map<String, Object> updates) {
    ObjectId objectId = new ObjectId(id);
    Playlist playlist = playlistRepository.findById(objectId)
        .orElseThrow(() -> new RuntimeException("Playlist not found"));

    if (!playlist.getUserId().equals(getCurrentUser().getId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

    List<String> allowedFields = List.of("name", "description", "thumbnail");

    updates.forEach((key, value) -> {
      if (allowedFields.contains(key)) {
        Field field = ReflectionUtils.findField(Playlist.class, key);
        if (field != null) {
          field.setAccessible(true);
          ReflectionUtils.setField(field, playlist, value);
        }
      }
    });

    playlistRepository.save(playlist);
    return ResponseEntity.ok(playlist);

  }

  // [PATCH] http://localhost:8081/api/common/playlist/{playlistId}/addSong
  // them 1 hoac nhieu bat hat vao playlist
  @PatchMapping("/{playlistId}/addSongs")
  public ResponseEntity<?> addSongToPlaylist(
      @PathVariable("playlistId") String playlistId,
      @RequestBody Map<String, List<String>> updates) {

    ObjectId playlistObjId = new ObjectId(playlistId);
    Playlist playlist = playlistRepository.findById(playlistObjId)
        .orElseThrow(() -> new RuntimeException("Playlist not found"));
    if (!playlist.getUserId().equals(getCurrentUser().getId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }
    List<String> newSongs = updates.get("songs");
    if (newSongs != null) {
      for (String song : newSongs) {
        if (!playlist.getSongs().contains(song)) {
          playlist.getSongs().add(song);
        }
      }
    }

    playlistRepository.save(playlist);
    return ResponseEntity.ok(playlist);
  }

  // [PATCH] http://localhost:8081/api/common/playlist/{playlistId}/removeSong
  // xoa 1 bai hat khoi playlist
  @PatchMapping("/{playlistId}/removeSongs")
  public ResponseEntity<?> removeSongsFromPlaylist(
      @PathVariable("playlistId") String playlistId,
      @RequestBody Map<String, List<String>> body) {

    ObjectId playlistObjId = new ObjectId(playlistId);
    Playlist playlist = playlistRepository.findById(playlistObjId)
        .orElseThrow(() -> new RuntimeException("Playlist not found"));

    if (!playlist.getUserId().equals(getCurrentUser().getId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

    List<String> songsToRemove = body.get("songs");
    if (songsToRemove != null && !songsToRemove.isEmpty()) {
      playlist.getSongs().removeAll(songsToRemove);
      playlistRepository.save(playlist);
    }

    return ResponseEntity.ok(playlist);
  }

  // [DELETE] http://localhost:8081/api/common/playlist/delete/{id}
  // xoá playlist
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<?> deletePlaylist(@PathVariable String id) {
    ObjectId objectId = new ObjectId(id);
    Optional<Playlist> playlistOpt = playlistRepository.findById(objectId);

    if (playlistOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Playlist playlist = playlistOpt.get();
    if (!playlist.getUserId().equals(getCurrentUser().getId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }

    playlistRepository.deleteById(objectId);
    return ResponseEntity.ok().build();
  }

}
