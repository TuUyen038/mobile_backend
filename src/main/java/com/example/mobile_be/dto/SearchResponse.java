package com.example.mobile_be.dto;

import java.util.List;

import com.example.mobile_be.models.Playlist;

import lombok.Data;

@Data
public class SearchResponse {
    private List<SongResponse> songs;
    private List<Playlist> playlists;
    private List<UserResponse> artists;
}

