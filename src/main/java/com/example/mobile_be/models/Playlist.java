package com.example.mobile_be.models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "playlist")

public class Playlist extends BaseDocument {
    @Id
    private ObjectId id;
    @TextIndexed
    private String name;
    private String description;
    private List<String> songs = new ArrayList<>();
    private String userId;
    private String thumbnailUrl;
    private Boolean isPublic;
    private String type;

    public String getId() {
        return id != null ? id.toHexString() : null;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String user_id) {
        this.userId = user_id;
    }
}
