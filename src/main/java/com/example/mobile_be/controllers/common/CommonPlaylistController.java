package com.example.mobile_be.controllers.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.mobile_be.dto.AddSongsRequest;
import com.example.mobile_be.dto.PlaylistRequest;
import com.example.mobile_be.dto.SongResponse;
import com.example.mobile_be.models.Playlist;
import com.example.mobile_be.models.Song;
import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.PlaylistRepository;
import com.example.mobile_be.repository.UserRepository;
import com.example.mobile_be.repository.SongRepository;
import com.example.mobile_be.security.UserDetailsImpl;
import com.example.mobile_be.service.ImageStorageService;

@RestController
@RequestMapping("/api/common/playlist")

public class CommonPlaylistController {
  @Autowired
  private PlaylistRepository playlistRepository;
  @Autowired
  UserRepository userRepository;
  @Autowired
  private ImageStorageService imageStorageService;
  @Autowired
  private SongRepository songRepository;
  @Autowired
  private MongoTemplate mongoTemplate;

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
    ObjectId objId;
    try {
      objId = new ObjectId(id);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("ID không hợp lệ");
    }

    Optional<Playlist> playlistOpt = playlistRepository.findById(objId);
    if (playlistOpt.isEmpty()) {

      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy playlist");
    }

    Playlist playlist = playlistOpt.get();
    if (!playlist.getUserId().equals(getCurrentUser().getId())) {

      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Người dùng không có quyền truy cập");
    }

    return ResponseEntity.ok(playlist);
  }

  // hàm lấy tất cả bài hát trong library của mình
  public Set<String> getAllSongIdsInUserLibrary(String userId) {
    Query query = new Query(Criteria.where("userId").is(userId));
    query.fields().include("songs");

    List<Playlist> playlists = mongoTemplate.find(query, Playlist.class);

    Set<String> songIdSet = new HashSet<>();
    for (Playlist p : playlists) {
      if (p.getSongs() != null) {
        songIdSet.addAll(p.getSongs());
      }
    }
    return songIdSet;
  }

  // lấy songs của playlist theo playlistId
  @GetMapping("/{playlistId}/songs")
  public ResponseEntity<?> getSongsInPlaylist(@PathVariable String playlistId) {
    ObjectId playlistObjId;
    try {
      playlistObjId = new ObjectId(playlistId);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body("Invalid playlist ID");
    }

    Playlist playlist = playlistRepository.findById(playlistObjId)
        .orElseThrow(() -> new RuntimeException("Playlist not found"));

    // Check quyền
    if (!playlist.getUserId().equals(getCurrentUser().getId()) && !playlist.getIsPublic()) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
    }
    

    List<ObjectId> songObjectIds = playlist.getSongs()
        .stream()
        .map(ObjectId::new)
        .toList();
    String myId = null;
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetailsImpl) {
      UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
      myId = userDetails.getId().toString();
    }
    Set<String> songIdsInLibrary = getAllSongIdsInUserLibrary(myId);

    List<Song> songsInDb = songRepository.findByIdIn(songObjectIds);

    // Tạo map để tra nhanh theo id
    Map<String, Song> songMap = songsInDb.stream()
        .collect(Collectors.toMap(song -> song.getId().toString(), Function.identity()));

    // Duyệt theo thứ tự gốc
    List<SongResponse> orderedSongs = new ArrayList<>();
    for (String songId : playlist.getSongs()) {
      Song song = songMap.get(songId);
      if (song == null)
        continue;

      SongResponse res = new SongResponse();
      res.setId(song.getId());
      res.setTitle(song.getTitle());
      res.setArtistId(song.getArtistId());
      res.setAudioUrl(song.getAudioUrl());
      res.setDuration(song.getDuration());
      res.setViews(song.getViews());
      res.setDescription(song.getDescription());
      res.setCoverImageUrl(song.getCoverImageUrl());
      res.setIsInLibrary(songIdsInLibrary.contains(song.getId()));
      orderedSongs.add(res);
    }

    return ResponseEntity.ok(orderedSongs);
  }

  // Lấy playlist dựa vào artistId (playlist do co isPublic = true)
  @GetMapping("/artist/{artistId}")
  public ResponseEntity<?> getPlaylistByArtistId(@PathVariable String artistId) {
    System.out.println("Artist ID: " + artistId);
    List<Playlist> pll = playlistRepository.findByUserIdAndIsPublicTrue(artistId);
    System.out.println("Found " + pll.size() + " playlists");

    if (pll.isEmpty()) {

      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy playlist");
    }

    return ResponseEntity.ok(pll);

  }

  // [GET] http://localhost:8081/api/common/playlist/search?keyword=...
  // tìm kiếm playlist theo tên
  @GetMapping("/search")
  public ResponseEntity<List<Playlist>> searchPlaylistByName(@RequestParam String name) {
    List<Playlist> playlists = playlistRepository.findByNameContainingIgnoreCase(name);
    return ResponseEntity.ok(playlists);
  }

  // [POST] http://localhost:8081/api/common/playlist/create
  // tạo playlist
  @PostMapping("/create")
  public ResponseEntity<?> postPlaylist(@ModelAttribute PlaylistRequest request) {
    // Lấy user đang đăng nhập từ SecurityContext
    try {

      User user = getCurrentUser();

      Playlist playlist = new Playlist();
      playlist.setName(request.getName());
      playlist.setDescription(request.getDescription());
      playlist.setUserId(user.getId());
      playlist.setIsPublic(false);

      MultipartFile thumbnail = request.getThumbnail();

      if (thumbnail != null && !thumbnail.isEmpty()) {
        String url = imageStorageService.saveFile(thumbnail, "playlists");
        playlist.setThumbnailUrl(url);

      }
      playlistRepository.save(playlist);
      return ResponseEntity.status(200).body("Playlist created successfully.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Loi o tao playlist: " + e.getMessage());
    }
  }

  // [PATCH] http://localhost:8081/api/common/playlist/change/{playlistId}
  // Chỉ bao gồm thay đổi name, thumbnail, description, thu tu bai hat
  // neu thay doi thuoc tinh khac thi mac du 200 OK nhung thuoc tinh do van k thay
  // doi
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

    // Xử lý thay đổi thứ tự bài hát
    if (updates.containsKey("songs")) {
      try {
        @SuppressWarnings("unchecked")
        List<String> newOrder = (List<String>) updates.get("songs");

        List<String> currentSongs = playlist.getSongs();
        // check newOder co du va dung songsId khong
        if (newOrder.size() != currentSongs.size() ||
            !new HashSet<>(newOrder).equals(new HashSet<>(currentSongs))) {
          return ResponseEntity.badRequest().body("Invalid song list: mismatched song IDs");
        }
        playlist.setSongs(newOrder);
      } catch (ClassCastException e) {
        return ResponseEntity.badRequest().body("Invalid song list format");
      }
    }

    playlistRepository.save(playlist);
    return ResponseEntity.ok(playlist);

  }

  // [PATCH] http://localhost:8081/api/common/playlist/addSong
  // them 1 hoac nhieu bat hat vao playlist
  @PostMapping("/addSongs")
  public ResponseEntity<?> addMultipleSongsToLibrary(@RequestBody AddSongsRequest request) {
    List<String> songIds = request.getSongs();
    String playlistId = request.getPlaylistId();

    if (songIds == null || songIds.isEmpty()) {
      return ResponseEntity.badRequest().body("Missing songIds");
    }

    User currentUser = getCurrentUser();
    String userId = currentUser.getId();

    Playlist targetPlaylist;

    if (playlistId != null && !playlistId.trim().isEmpty()) {
      // TH1: Người dùng chỉ định playlist
      targetPlaylist = playlistRepository.findById(new ObjectId(playlistId))
          .orElseThrow(() -> new RuntimeException("Playlist not found"));
      if (!targetPlaylist.getUserId().equals(userId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
      }
    } else {
      // TH2: Không có playlistId → dùng "Favorites"
      System.out.println(userId);
      Optional<Playlist> optional = playlistRepository.findByUserIdAndName(userId, "Favorites");
      if (optional.isPresent()) {
        targetPlaylist = optional.get();
      } else {
        targetPlaylist = new Playlist();
        targetPlaylist.setName("Favorites");
        targetPlaylist.setUserId(userId);
        targetPlaylist.setIsPublic(false);
        targetPlaylist.setThumbnailUrl("/uploads/playlists/default-img.jpg");
        targetPlaylist.setDescription("A list of your favorite songs");
        targetPlaylist.setSongs(new ArrayList<>());

        playlistRepository.save(targetPlaylist);
      }
    }

    for (String songId : songIds) {
      if (!targetPlaylist.getSongs().contains(songId)) {
        targetPlaylist.getSongs().add(songId);
      }
    }
    System.out.println("hiiiiiiiiiiiii");

    playlistRepository.save(targetPlaylist);
    return ResponseEntity.ok("Songs added successfully");
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
