package com.example.mobile_be.service;

import java.util.List;

import com.example.mobile_be.models.Playlist;
import com.example.mobile_be.repository.PlaylistRepository;
import com.example.mobile_be.repository.SongRepository;

public class PlaylistService {

    private final PlaylistRepository playlistRepository;

    public PlaylistService(PlaylistRepository p) {
        playlistRepository = p;
    }

    public List<Playlist> searchByName(String name) {
        return playlistRepository.findByNameContainingIgnoreCase(name);
    }
}
