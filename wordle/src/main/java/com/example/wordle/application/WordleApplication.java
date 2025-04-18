package com.example.wordle.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.wordle")
@EnableJpaRepositories(basePackages = "com.example.wordle.repository")
@EntityScan(basePackages = "com.example.wordle.model")
public class WordleApplication {

	public static void main(String[] args) {
		SpringApplication.run(WordleApplication.class, args);
	}

}
