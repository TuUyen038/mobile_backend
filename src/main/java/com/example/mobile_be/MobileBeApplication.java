package com.example.mobile_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableMongoAuditing
public class MobileBeApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
                              .directory("./") // thư mục gốc
                              .load();
System.out.println("🔍 MONGODB_URI = " + System.getenv("MONGODB_URI"));

        System.setProperty("PORT", dotenv.get("PORT"));
        System.setProperty("MONGODB_URI", dotenv.get("MONGODB_URI"));
        System.setProperty("MONGODB_DATABASE", dotenv.get("MONGODB_DATABASE"));
        System.setProperty("MAIL_USERNAME", dotenv.get("MAIL_USERNAME"));
        System.setProperty("MAIL_PASSWORD", dotenv.get("MAIL_PASSWORD"));
        System.setProperty("UPLOAD_DIR", dotenv.get("UPLOAD_DIR"));
		SpringApplication.run(MobileBeApplication.class, args);
	}

}
