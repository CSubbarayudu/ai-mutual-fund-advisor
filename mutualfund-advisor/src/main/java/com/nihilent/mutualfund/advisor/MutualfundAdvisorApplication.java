package com.nihilent.mutualfund.advisor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MutualfundAdvisorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MutualfundAdvisorApplication.class, args);
	}

}
