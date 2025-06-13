package com.example.mobile_be.models;

import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "playlist")

public class Playlist extends BaseDocument implements MultiResponse{
    
    @Override
    public String getType() {
        return "playlist";
    }
    @Id
    private ObjectId id;
    private String name;
    private String description;
    private ArrayList<String> songs = new ArrayList<>();
    private String userId;
    private String thumbnailUrl;
    private Boolean isPublic;
    

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
