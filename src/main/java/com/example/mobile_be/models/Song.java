package com.example.mobile_be.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)

@Document(collection = "song")

public class Song extends BaseDocument {
    @Id
    private ObjectId id;
    private String artist_id;
    private String description;
    private String title;
    private String audioUrl;
    private String coverImageUrl;
    private List<String> genreId=  new ArrayList<>();
    private Boolean isApproved;
    private Boolean isPublic;
    private String lyric;
    private Double duration;
    private Double views;

    public String getId() {
        return id != null ? id.toHexString() : null;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
