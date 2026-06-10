package com.ism.rhconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RhconnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(RhconnectApplication.class, args);
	}

}
