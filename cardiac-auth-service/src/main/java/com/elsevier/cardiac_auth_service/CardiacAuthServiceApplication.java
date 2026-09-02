package com.elsevier.cardiac_auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CardiacAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardiacAuthServiceApplication.class, args);
	}

}
