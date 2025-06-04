package com.example.mobile_be.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = false)

@Document(collection = "artistRequest")

public class ArtistRequest {
    @Id
    private ObjectId id;
    private String userId;
    private String portfolioUrl;
    private String status;
    private String reviewedBy;
    private Instant submittedAt;
    private Instant reviewedAt;

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
