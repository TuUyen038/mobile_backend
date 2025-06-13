package com.example.mobile_be.controllers.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mobile_be.dto.SearchResponse;
import com.example.mobile_be.dto.SongResponse;
import com.example.mobile_be.dto.UserResponse;
import com.example.mobile_be.models.MultiResponse;
import com.example.mobile_be.models.Playlist;
import com.example.mobile_be.models.Song;
import com.example.mobile_be.models.User;
import com.example.mobile_be.repository.PlaylistRepository;
import com.example.mobile_be.repository.SongRepository;
import com.example.mobile_be.repository.UserRepository;
import com.example.mobile_be.security.UserDetailsImpl;
import com.example.mobile_be.service.SongService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/common/song")
public class CommonSongController {
    private final SongService songService;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final PlaylistRepository playlistRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSongById(@PathVariable ObjectId id) {
        Optional<Song> songOpt = songService.getSongById(id);
        if (songOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Song not found!");
        }
        return ResponseEntity.ok(songOpt.get());
    }

    // stream file .mp3
    @GetMapping("/stream/{id}")
    public void streamSong(@PathVariable("id") ObjectId id, HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        Optional<Song> test = songService.getSongById(id);
        if (test.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("Song not found!!");
            return;
        }

        File songFile = new File(test.get().getAudioUrl());
        if (!songFile.exists()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("File not found!!");
            return;
        }

        // views++
        songService.incrementViews(id);

        long fileLength = songFile.length();
        String range = req.getHeader("Range");

        long start = 0;
        long end = fileLength - 1;

        if (range != null && range.startsWith("bytes=")) {
            try {
                String[] ranges = range.substring(6).split("-");
                start = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    end = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            if (end >= fileLength) {
                end = fileLength - 1;
            }

            res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206
        } else {
            res.setStatus(HttpServletResponse.SC_OK); // Full download
        }

        long contentLength = end - start + 1;

        res.setContentType("audio/mpeg");
        res.setHeader("Accept-Ranges", "bytes");
        res.setHeader("Content-Length", String.valueOf(contentLength));
        res.setHeader("Content-Range", String.format("bytes %d-%d/%d", start, end, fileLength));
        res.setHeader("Content-Disposition", "inline; filename=\"" + songFile.getName() + "\"");

        try (InputStream inputStream = new FileInputStream(songFile);
                OutputStream outputStream = res.getOutputStream()) {
            inputStream.skip(start);
            byte[] buffer = new byte[8192];
            long bytesRemaining = contentLength;

            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1 && bytesRemaining > 0) {
                if (bytesRead > bytesRemaining) {
                    bytesRead = (int) bytesRemaining;
                }
                outputStream.write(buffer, 0, bytesRead);
                bytesRemaining -= bytesRead;
            }
        }
    }

    @GetMapping("/new-release")
    public ResponseEntity<?> getNewReleaseSongs() {
        List<Song> newSongs = songService.getNewReleaseSongs();
        return ResponseEntity.ok(newSongs);
    }

    // @GetMapping("/recently")
    // public ResponseEntity<?> getRecentlyPlayedSongs() {
    // List<Song> recentSongs = songService.getRecentlyPlayedSongs();
    // return ResponseEntity.ok(recentSongs);
    // }

    // response trả về song dựa trên title của song hoặc tên của artist
    @GetMapping("/search")
    public ResponseEntity<?> searchSongsByKeyword(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<Song> songsByTitle = songRepository.findByTitleContainingIgnoreCase(keyword);

        List<User> artists = userRepository.findByFullNameContainingIgnoreCase(keyword);
        List<String> artistIds = artists.stream().map(User::getId).collect(Collectors.toList());
        List<Song> songsByArtist = songRepository.findByArtistIdIn(artistIds);

        Map<String, Song> songMap = new HashMap<>();
        songsByTitle.forEach(song -> songMap.put(song.getId(), song));
        songsByArtist.forEach(song -> songMap.putIfAbsent(song.getId(), song));
        List<Song> matchedSongs = new ArrayList<>(songMap.values());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String myId = userDetails.getId().toString();

        List<Playlist> userPlaylists = playlistRepository.findByUserId(myId);

        // Map songId -> List<playlistId>
        Map<String, List<String>> songToPlaylistMap = new HashMap<>();
        for (Playlist playlist : userPlaylists) {
            for (String songId : playlist.getSongs()) {
                songToPlaylistMap.computeIfAbsent(songId, k -> new ArrayList<>()).add(playlist.getId());
            }
        }

        List<SongResponse> responses = matchedSongs.stream().map(song -> {
            SongResponse res = new SongResponse();
            res.setId(song.getId());
            res.setTitle(song.getTitle());
            res.setArtistId(song.getArtistId());
            res.setAudioUrl(song.getAudioUrl());
            res.setDuration(song.getDuration());
            res.setViews(song.getViews());
            res.setDescription(song.getDescription());
            res.setCoverImageUrl(song.getCoverImageUrl());
            res.setPlaylistIds(songToPlaylistMap.getOrDefault(song.getId(), List.of()));
            return res;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // response trả về playlist, artist, song
    @GetMapping("/search/multi")
    public ResponseEntity<?> searchAll(@RequestParam("keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.ok(new SearchResponse());
        }

        List<Song> songsByTitle = songRepository.findByTitleContainingIgnoreCase(keyword);

        List<UserResponse> artists = userRepository.findByFullNameContainingIgnoreCaseAndIsVerifiedArtistTrue(keyword)
                .stream()
                .map(art -> {
                    UserResponse res = new UserResponse();
                    res.setId(art.getId());
                    res.setEmail(art.getEmail());
                    res.setFullName(art.getFullName());
                    res.setRole(art.getRole());
                    res.setAvatarUrl(art.getAvatarUrl());
                    res.setIsVerifiedArtist(art.getIsVerifiedArtist());
                    res.setIsVerified(art.getIsVerified());
                    return res;
                })
                .collect(Collectors.toList());

        List<Playlist> playlists = playlistRepository.findByNameContainingIgnoreCase(keyword);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String myId = userDetails.getId().toString();

        List<Playlist> userPlaylists = playlistRepository.findByUserId(myId);

        Map<String, List<String>> songToPlaylistMap = new HashMap<>();
        for (Playlist playlist : userPlaylists) {
            for (String songId : playlist.getSongs()) {
                songToPlaylistMap.computeIfAbsent(songId, k -> new ArrayList<>()).add(playlist.getId());
            }
        }

        List<SongResponse> songs = songsByTitle.stream().map(song -> {
            SongResponse res = new SongResponse();
            String songId = song.getId();
            res.setId(songId);
            res.setTitle(song.getTitle());
            res.setArtistId(song.getArtistId());
            res.setAudioUrl(song.getAudioUrl());
            res.setDuration(song.getDuration());
            res.setViews(song.getViews());
            res.setDescription(song.getDescription());
            res.setCoverImageUrl(song.getCoverImageUrl());
            res.setPlaylistIds(songToPlaylistMap.getOrDefault(songId, List.of()));
            return res;
        }).collect(Collectors.toList());

        //sort 
        List<MultiResponse> mixed = new ArrayList<>();
        int maxSize = Math.max(songs.size(), Math.max(playlists.size(), artists.size()));
        for (int i = 0; i < maxSize; i++) {
            if (i < songs.size())
                mixed.add(songs.get(i));
            if (i < artists.size())
                mixed.add(artists.get(i));
            if (i < playlists.size())
                mixed.add(playlists.get(i));
        }
        return ResponseEntity.ok(mixed);

    }

    // filter
    @GetMapping("/filter")
    public ResponseEntity<?> filterSongsByCreatedAt(@RequestParam String filter) {
        List<Song> results;
        switch (filter) {
            case "newest":
                results = songRepository.findAllByOrderByCreatedAtDesc();
                break;
            case "trending":
                results = songRepository.findByOrderByViewsDesc();
                break;
            case "all":
                results = songRepository.findAll();
                break;
            default:
                return ResponseEntity.badRequest().body("Invalid filter: use 'newest', 'trending' or 'all'");
        }
        return ResponseEntity.ok(results);
    }

    // get songs by genre
    @GetMapping("/genre/{genreId}")
    public ResponseEntity<?> getSongsByGenre(@PathVariable("genreId") String genreId) {
        List<Song> songs = songRepository.findByGenreId(genreId);
        if (songs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No songs found for this genre.");
        }
        return ResponseEntity.ok(songs);
    }

}
