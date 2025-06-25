// package com.example.mobile_be.config;

    
// import jakarta.annotation.PostConstruct;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Sort;
// import org.springframework.data.mongodb.core.MongoTemplate;
// import org.springframework.data.mongodb.core.index.Index;
// import org.springframework.data.mongodb.core.index.TextIndexDefinition;
// import org.springframework.stereotype.Component;

// @Component
// @RequiredArgsConstructor
// public class IndexMigration {


//     private final MongoTemplate mongoTemplate;

//     @PostConstruct
//     public void initIndexes() {
//         TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
//                 .onField("title")
//                 .onField("description")
//                 .named("song_text_index")
//                 .build();

//         mongoTemplate.indexOps("song").ensureIndex(textIndex);
//     }
// }
