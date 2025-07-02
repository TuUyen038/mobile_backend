package com.example.mobile_be.models;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)

@Document(collection = "feedback")

public class Feedback extends BaseDocument {
    @Id
    private ObjectId id;
    private String userId;
    private String content;
    private String status;
    private String adminReply;


    public String getId() {
        return id != null ? id.toHexString() : null;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
