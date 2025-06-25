package com.example.mobile_be.dto;

import java.util.List;

import lombok.Data;

@Data
public class AddSongsRequest {
    private List<String> songs;
    private String playlistId;
}
