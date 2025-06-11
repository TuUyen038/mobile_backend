package com.example.mobile_be.controllers.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mobile_be.models.Song;
import com.example.mobile_be.repository.SongRepository;
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
    public void streamSong(@PathVariable ObjectId id, HttpServletRequest req, HttpServletResponse res)
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

        //views++
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

    @GetMapping("/search")
    public ResponseEntity<?> searchSongsByTitle(@RequestParam String title) {
        List<Song> results = songService.searchSongsByTitle(title);
        return ResponseEntity.ok(results);
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
}
