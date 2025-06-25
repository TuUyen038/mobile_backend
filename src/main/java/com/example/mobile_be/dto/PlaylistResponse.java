package com.example.mobile_be.dto;

import java.util.ArrayList;

import lombok.Data;

@Data
public class PlaylistResponse {
    private String id;
    private String name;
    private String description;
    private ArrayList<String> songs = new ArrayList<>();
    private String thumbnailUrl;
    private Boolean isInLibrary;
}
