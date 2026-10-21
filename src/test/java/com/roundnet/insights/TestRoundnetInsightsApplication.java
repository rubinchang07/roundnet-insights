package com.roundnet.insights;

import org.springframework.boot.SpringApplication;

public class TestRoundnetInsightsApplication {

	public static void main(String[] args) {
		SpringApplication.from(RoundnetInsightsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
