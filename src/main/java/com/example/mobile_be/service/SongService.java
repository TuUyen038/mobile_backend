package com.example.mobile_be.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.mobile_be.models.Song;
import com.example.mobile_be.repository.SongRepository;
import com.mongodb.client.result.UpdateResult;

@Service
public class SongService {

    private final MongoTemplate mongoTemplate;
    private final SongRepository songRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public SongService(SongRepository songRepository, MongoTemplate mongoTemplate) {
        this.songRepository = songRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Optional<Song> getSongById(ObjectId id) {
        return songRepository.findById(id);
    }

    public List<Song> searchSongsByTitle(String title) {
        return songRepository.findByTitleContainingIgnoreCase(title);
    }

    public void incrementViews(ObjectId id) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().inc("views", 1);
        UpdateResult result = mongoTemplate.updateFirst(query, update, Song.class);
    }

    public Song saveSongFile(Song song, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file.");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFileName.contains("..")) {
            throw new IllegalArgumentException("Invalid file name.");
        }

        String lower = originalFileName.toLowerCase();
        if (!lower.endsWith(".mp3") && !lower.endsWith(".lrc")) {
            throw new IllegalArgumentException("Only .mp3 and .lrc files are supported.");
        }

        String extension = lower.substring(lower.lastIndexOf("."));
        String subDir = extension.equals(".mp3") ? "songs" : "lyrics";
        Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir, subDir);
        Files.createDirectories(uploadPath);

        // Tạo tên file không trùng
        String fileName = originalFileName;
        Path filePath = uploadPath.resolve(fileName);
        int count = 1;
        while (Files.exists(filePath)) {
            String name = originalFileName.substring(0, originalFileName.lastIndexOf("."));
            fileName = name + "(" + count++ + ")" + extension;
            filePath = uploadPath.resolve(fileName);
        }

        Files.copy(file.getInputStream(), filePath);

        String fileUrl = "/" + uploadDir + "/" + subDir + "/" + fileName;
        if (extension.equals(".mp3")) {
            song.setAudioUrl(fileUrl);
        } else if (extension.equals(".lrc")) {
            song.setLyricUrl(fileUrl);
        }

        return songRepository.save(song);
    }
}
