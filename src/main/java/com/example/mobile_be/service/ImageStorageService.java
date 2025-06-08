package com.example.mobile_be.service;

    

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ImageStorageService {


    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file, String subFolder) throws IOException {
    String basePath = System.getProperty("user.dir"); // Thư mục gốc chạy app
    String finalPath = basePath + File.separator + uploadDir + File.separator + subFolder;

    File folder = new File(finalPath);
    if (!folder.exists()) {
        folder.mkdirs();
    }

    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
    String fullPath = finalPath + File.separator + fileName;

    file.transferTo(new File(fullPath));

    return "/uploads/" + subFolder + "/" + fileName;
}

}
