package com.audenyo.oidcplayground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OidcPlaygroundApplication {

	public static void main(String[] args) {
		SpringApplication.run(OidcPlaygroundApplication.class, args);
	}

}
