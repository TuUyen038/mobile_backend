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
    private String name;
    private String role;
    private String avatar_url;
    private ArrayList<String> favorite_song;
    public String getId() {
        return id != null ? id.toHexString() : null;
    }
    public void setId(ObjectId id) {
        this.id = id;
    }
}
