package com.thecritics.reorder;

import org.springframework.boot.SpringApplication;
import org.springframework.test.context.ActiveProfiles;

public class TestReorderApplication {

	public static void main(String[] args) {
		SpringApplication.from(ReorderApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
