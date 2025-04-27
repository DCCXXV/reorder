package com.thecritics.reorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EntityScan("com.thecritics.reorder.model")
public class ReorderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReorderApplication.class, args);
	}
}
