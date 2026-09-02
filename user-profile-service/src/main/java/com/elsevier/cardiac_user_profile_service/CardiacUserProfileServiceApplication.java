package com.elsevier.cardiac_user_profile_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CardiacUserProfileServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardiacUserProfileServiceApplication.class, args);
	}
}
