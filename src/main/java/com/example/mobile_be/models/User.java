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

@Document(collection = "user")
public class User extends BaseDocument {
    
    @Id
    private ObjectId id;
    private String email;
    private String password;
    private String lastName;
    @TextIndexed
    private String fullName;
    private String role;
    private String avatarUrl;
    private String bio;
    private Boolean isVerifiedArtist;
    private Boolean isVerified;
    private String resetToken;
    private List<String> recentlyPlayed = new ArrayList<>();

    public ObjectId getObjectId() {
        return id;
    }

    public String getId() {
        return id != null ? id.toHexString() : null;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
