package com.EnSe.Movienatic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MovienaticApplication {
	public static void main() {
		SpringApplication.run(MovienaticApplication.class);
	}

}
