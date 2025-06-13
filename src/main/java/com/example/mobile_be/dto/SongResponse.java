package com.example.mobile_be.dto;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SongResponse {
    private String id;
    private String artistId;
    private String audioUrl;
    private String title;
    private String description;
    private String coverImageUrl;

    private List<String> playlistIds = new ArrayList<>();
    private Long views = 0l;
    private Double duration;

}
