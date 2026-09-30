package com.EnSe.Movienatic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.EnSe.Movienatic.repository.UserRepository;

@SpringBootApplication
public class MovienaticApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovienaticApplication.class, args);
	}

}
