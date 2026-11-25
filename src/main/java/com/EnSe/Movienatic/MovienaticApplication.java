package com.EnSe.Movienatic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableScheduling
@EnableResilientMethods
public class MovienaticApplication {
	public static void main() {
		SpringApplication.run(MovienaticApplication.class);
	}

}
