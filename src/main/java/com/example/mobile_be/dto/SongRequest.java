package com.example.mobile_be.dto;


import java.util.ArrayList;

import org.bson.types.ObjectId;

import lombok.Data;

@Data
public class SongRequest {
    private String artistId;
    private String title;
    private String description;
    private String coverImageUrl;
    private ArrayList<String> genreId;
}
