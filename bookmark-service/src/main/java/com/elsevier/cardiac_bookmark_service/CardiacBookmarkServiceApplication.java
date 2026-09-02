package com.elsevier.cardiac_bookmark_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CardiacBookmarkServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardiacBookmarkServiceApplication.class, args);
	}
}
