package com.habittracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.habittracker.repository")
public class HabitTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HabitTrackerApplication.class, args);
	}

}
