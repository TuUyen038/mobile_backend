package com.example.mobile_be.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.mobile_be.models.Song;
import com.example.mobile_be.repository.SongRepository;

//Luu file .mp3 vao /uploads
@Service
public class SongService {
    private static final String UPLOAD_DIR = "uploads/";
    private final SongRepository songRepository;

    public SongService(SongRepository s) {
        songRepository = s;
    }

    public Optional<Song> getSongById(ObjectId id) {
        return songRepository.findById(id);
    }

    public String getUploadDir() {
        return UPLOAD_DIR;
    }

    public List<Song> getNewReleaseSongs() {
        return songRepository.findTop10ByOrderByCreatedAtDesc();
    };

    // public List<Song> getRecentlyPlayedSongs(){
    // return songRepository.findTop10ByOrderByLastPlayedAtDesc();
    // };

    public List<Song> searchSongsByTitle(String title) {
        return songRepository.findByTitleContainingIgnoreCase(title);
    }

    public Song saveSongFile(Song song, MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file.");
        }
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        if (!originalFileName.toLowerCase().endsWith(".mp3") || originalFileName.contains("..")) {
            throw new IllegalArgumentException("Invalid file name.");
        }

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath))
            Files.createDirectories(uploadPath);
        String fileName = originalFileName;
        Path filePath = uploadPath.resolve(fileName);

        // neu ten trung thi them (count) o dang sau
        int count = 1;
        while (Files.exists(filePath)) {
            String name = originalFileName.substring(0, originalFileName.lastIndexOf("."));
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            fileName = name + "(" + count++ + ")" + extension;
            filePath = uploadPath.resolve((fileName));
        }

        Files.copy(file.getInputStream(), filePath);

        // ghi de len file trung ten truoc do luon
        // Files.copy(file.getInputStream(), filePath,
        // StandardCopyOption.REPLACE_EXISTING);

        song.setAudioUrl(UPLOAD_DIR + fileName);
        songRepository.save(song);
        return songRepository.save(song);
    }
}
