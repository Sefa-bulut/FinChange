package com.example.finchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableJpaAuditing(dateTimeProviderRef = "customDateTimeProvider")
@EnableScheduling
@EnableAsync
public class FinchangeApplication {

	public static void main(String[] args) {

		SpringApplication.run(FinchangeApplication.class, args);
	}

}