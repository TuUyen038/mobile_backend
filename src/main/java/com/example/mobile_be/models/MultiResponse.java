package com.example.mobile_be.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MultiResponse {
    private String type; // "song", "artist", "playlist"
    private Object data;
}
