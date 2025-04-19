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
@Document(collection = "playlist")

public class Playlist extends BaseDocument {
 @Id
 private ObjectId id;
 private String name;
 private String description;
 private List<String> songs = new ArrayList<>();
 private String user_id;

 public String getId() {
     return id!= null ? id.toHexString() : null;
 }
 public void setId(ObjectId id) {
     this.id = id;
 }
}

