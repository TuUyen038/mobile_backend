package com.example.mobile_be.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import java.util.ArrayList;

@Data
@EqualsAndHashCode(callSuper = false)

@Document(collection = "comment")
public class Comment extends BaseDocument {
  @Id
  private ObjectId id;
  private ObjectId user_id;
  private String content;
  private ArrayList<ObjectId> admin_reply;
  private ObjectId song_id;
  private String status;

  public String getId() {
    return id != null ? id.toHexString() : null;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }
}
