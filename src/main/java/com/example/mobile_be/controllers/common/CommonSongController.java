package com.example.mobile_be.controllers.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mobile_be.models.Song;
import com.example.mobile_be.service.SongService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/common/song")
public class CommonSongController {
    private final SongService songService;

    public CommonSongController(SongService s) {
        songService = s;
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
    public void streamSong(@PathVariable ObjectId id, HttpServletResponse res) throws IOException {
        Optional<Song> test = songService.getSongById(id);

        if (test.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("Song not found!!");
            return;
        }
        Song song = test.get();
        File songFile = new File(song.getAudioUrl());

        if (!songFile.exists()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("File not found!!");
            return;
        }

        res.setContentType("audio/mpeg");
        res.setHeader("Content-Disposition", "inline; filename=\"" + songFile.getName() + "\"");

        try (InputStream iStream = new FileInputStream(songFile); OutputStream oStream = res.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = iStream.read(buffer)) != -1) {
                oStream.write(buffer, 0, bytesRead);
            }
            oStream.flush();
        }
    }

}
