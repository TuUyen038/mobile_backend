package com.example.mobile_be.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class GenreRequest {
    private String name;
    private String description;
    private MultipartFile thumbnail;

}
