package com.example.mobile_be.dto;

import java.util.ArrayList;

import lombok.Data;

@Data
public class PlaylistRequest {
    private String name;
    private String description;
    private ArrayList<String> songs = new ArrayList<>();
    private String thumbnailUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
