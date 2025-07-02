package com.example.mobile_be.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;


import com.example.mobile_be.models.Song;
import com.example.mobile_be.repository.SongRepository;
import com.mongodb.client.result.UpdateResult;

//Luu file .mp3 vao /uploads
@Service
public class SongService {
    @Autowired
private MongoTemplate mongoTemplate;

    private static final String UPLOAD_DIR = "uploads/";
    private final SongRepository songRepository;

    public SongService(SongRepository s) {
        songRepository = s;
    }

    public void incrementViews(ObjectId id) {
    Query query = new Query(Criteria.where("_id").is(id));
    Update update = new Update().inc("views", 1);
    UpdateResult result = mongoTemplate.updateFirst(query, update, Song.class);
    System.out.println("Modified count: " + result.getModifiedCount()); 
}

    public Optional<Song> getSongById(ObjectId id) {
        return songRepository.findById(id);
    }

    public String getUploadDir() {
        return UPLOAD_DIR;
    }

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

    if (originalFileName.contains("..")) {
        throw new IllegalArgumentException("Invalid file name.");
    }

    String lower = originalFileName.toLowerCase();
    if (!lower.endsWith(".mp3") && !lower.endsWith(".lrc")) {
        throw new IllegalArgumentException("Only .mp3 and .lrc files are supported.");
    }

    String extension = lower.substring(lower.lastIndexOf("."));
    String subDir = extension.equals(".mp3") ? "songs" : "lyrics";
    Path uploadPath = Paths.get(UPLOAD_DIR, subDir);
    Files.createDirectories(uploadPath);

    String fileName = originalFileName;
    Path filePath = uploadPath.resolve(fileName);

    int count = 1;
    while (Files.exists(filePath)) {
        String name = originalFileName.substring(0, originalFileName.lastIndexOf("."));
        fileName = name + "(" + count++ + ")" + extension;
        filePath = uploadPath.resolve(fileName);
    }

    Files.copy(file.getInputStream(), filePath);

    String fileUrl = "/" + UPLOAD_DIR + "/" + subDir + "/" + fileName;
    if (extension.equals(".mp3")) {
        song.setAudioUrl(fileUrl);
    } else if (extension.equals(".lrc")) {
        song.setLyricUrl(fileUrl);
    }

    return songRepository.save(song);
}

}
