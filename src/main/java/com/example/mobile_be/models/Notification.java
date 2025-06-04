package com.example.mobile_be.models;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Document(collection = "notification")

public class Notification {
  @Id
  private ObjectId id;
  private String userId;
  private String type; // newSong, replyFeedback, comment
  private String content;
  private boolean isRead;

  public String getId() {
    return id != null ? id.toHexString() : null;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
