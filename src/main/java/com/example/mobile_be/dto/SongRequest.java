package com.example.mobile_be.dto;


import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class SongRequest {
    private String artistId;
    private String title;
    private String description;
    private MultipartFile coverImage;
    private ArrayList<String> genreId;

}
