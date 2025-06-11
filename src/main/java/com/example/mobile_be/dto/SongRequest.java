package com.example.mobile_be.dto;


import java.util.ArrayList;


import lombok.Data;

@Data
public class SongRequest {
    private String artist_id;
    private String title;
    private String description;
    private String coverImageUrl;
    private ArrayList<String> genreId;
}
