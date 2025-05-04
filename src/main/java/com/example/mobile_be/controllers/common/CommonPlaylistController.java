package com.example.mobile_be.controllers.common;

import com.example.mobile_be.models.Playlist;
import com.example.mobile_be.repository.PlaylistRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/common/playlist")

public class CommonPlaylistController {
 @Autowired
 private PlaylistRepository playlistRepository;

 // [GET] http://localhost:8081/api/common/playlist
 // lấy tất cả playlist
 @GetMapping
 public List<Playlist> getAllPlaylists() {
  return playlistRepository.findAll();
 }


 //[GET] http://localhost:8081/api/common/playlist/{playlistId}
 // lấy playlist theo ID
@GetMapping("/{id}")
 public ResponseEntity<?> getPlaylistById(@PathVariable ("id") String id) {
   try {
    ObjectId objId = new ObjectId(id);
    Optional<Playlist> playlist = playlistRepository.findById(objId);

    return playlist.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

   }catch (Exception e) {
    e.printStackTrace();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Đã có lỗi o getbyId: " + e.getMessage());
   }
  }


  //[GET] http://localhost:8081/api/commonplaylist/search?keyword=...
  // tìm kiếm playlist theo tên
  @GetMapping("/search")
  public ResponseEntity<List<Playlist>> searchPlaylistByName(@RequestParam String keyword) {
    List<Playlist> playlists = playlistRepository.findByNameContainingIgnoreCase(keyword);
    return ResponseEntity.ok(playlists);
  }
  

 // [POST] http://localhost:8081/api/playlist/create
 // tạo playlist
 @PostMapping("/create")
 public Playlist postUser(@RequestBody Playlist playlist) {
  return playlistRepository.save(playlist);
 }


 // [PATCH] http://localhost:8081/api/playlist/change/{playlistId}
 // Chỉ bao gồm thay đổi name, thumbmail, description
 //neu thay doi thuoc tinh khac thi mac du 200 OK nhung thuoc tinh do van k bi thay doi
 @PatchMapping("/change/{id}")
 public ResponseEntity<Playlist> updateUser(@PathVariable ("id") String id, @RequestBody Map<String, Object> updates) {
  ObjectId objectId = new ObjectId(id);
  Playlist playlist = playlistRepository.findById(objectId)
    .orElseThrow(() -> new RuntimeException("Playlist not found"));

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


 //[PATCH]  http://localhost:8081/api/playlist/{playlistId}/addSong
 //them 1 bat hat vao playlist
 @PatchMapping("/{playlistId}/addSong") 
 public ResponseEntity<Playlist> addSongToPlaylist (@PathVariable ("playlistId") String playlistId,@RequestBody Map<String, String> updates) {
  ObjectId playlistObjId = new ObjectId(playlistId);
  Playlist playlist = playlistRepository.findById(playlistObjId).orElseThrow(() -> 
   new RuntimeException("Playlist not found"));
   
   if(updates.containsKey("songs")) {
    String newSong = updates.get("songs");
    if(!playlist.getSongs().contains(newSong)) {
     playlist.getSongs().add(newSong);
    } 
   }
   playlistRepository.save(playlist);
   return ResponseEntity.ok(playlist);
 }


 //[PATCH]  http://localhost:8081/api/common/playlist/{playlistId}/removeSong
 //xoa 1 bai hat khoi playlist
 @PatchMapping("/{playlistId}/removeSong/{removeId}") 
 public ResponseEntity<Playlist> RemoveSongFromPlaylist (@PathVariable ("playlistId") String playlistId, @PathVariable ("removeId") String removeId) {
  ObjectId playlistObjId = new ObjectId(playlistId);

  Playlist playlist = playlistRepository.findById(playlistObjId).orElseThrow(() -> 
   new RuntimeException("Playlist not found"));

   if(playlist.getSongs().contains(removeId)) {
    playlist.getSongs().remove(removeId);

    playlistRepository.save(playlist);
   }
   return ResponseEntity.ok(playlist);
 }


 // [DELETE] http://localhost:8081/api/common/playlist/delete/{id}
 // xoá playlist
 @DeleteMapping("/delete/{id}")
 public ResponseEntity<Void> deleteUser(@PathVariable String id) {
  ObjectId objectId = new ObjectId(id);

  if (playlistRepository.existsById(objectId)) {
   playlistRepository.deleteById(objectId);
   return ResponseEntity.ok().build();
  } else {
   return ResponseEntity.notFound().build();
  }
 }

}
