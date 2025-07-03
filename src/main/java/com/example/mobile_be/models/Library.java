package com.example.mobile_be.models;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)

@Document(collection = "library")

public class Library extends BaseDocument {
    @Id
    private ObjectId id;
    private String userId;
    private List<String> playlistIds = new ArrayList<>();


  public String getId() {
    return id != null ? id.toHexString() : null;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }
}