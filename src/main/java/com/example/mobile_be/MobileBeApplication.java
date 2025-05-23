package com.example.mobile_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class MobileBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(MobileBeApplication.class, args);
	}

}
