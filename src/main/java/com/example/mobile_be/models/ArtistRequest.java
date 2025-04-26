package com.example.mobile_be.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import java.sql.Date;

@Data
@EqualsAndHashCode(callSuper = false)

@Document(collection = "artistRequest")

public class ArtistRequest {
    @Id
    private ObjectId id;
    private ObjectId user_id;
    private String portfolioUrl;
    private String status;
    private ObjectId reviewedBy;
    private Date submittedAt;
    private Date reviewedAt;

    public String getId() {
        return id != null ? id.toHexString() : null;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
