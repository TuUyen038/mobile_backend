package com.example.mobile_be.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.lang.reflect.Array;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)

@Document(collection = "song")

public class Song extends BaseDocument {

    @Id
    private ObjectId id;
    private String artistId;
    private String description;
    
    @TextIndexed 
    private String title;
    private String audioUrl;
    private String coverImageUrl;
    @Indexed
    private ArrayList<String> genreId=  new ArrayList<>();
    private Boolean isApproved;
    private Boolean isPublic;
    private String lyric;
    private Double duration;
    private Long views = 0l;
    private Date lastPlayedAt;

    public String getId() {
        return id != null ? id.toHexString() : null;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
