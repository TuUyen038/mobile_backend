package com.example.mobile_be.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.data.mongodb.core.mapping.Document;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;

@Data
@EqualsAndHashCode(callSuper = false)

@Document(collection = "user")
public class User extends BaseDocument {
    @Id
    private ObjectId id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private String avatarUrl;
    private String bio;
    private Boolean isVerifiedArtist;
    private String resetToken;
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
