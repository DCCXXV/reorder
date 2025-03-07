package com.thecritics.reorder;

import org.springframework.boot.SpringApplication;

public class TestReorderApplication {

	public static void main(String[] args) {
		SpringApplication.from(ReorderApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
