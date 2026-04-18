package com.todo.smarttodo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
//Runs the method automatically at fixed intervals.
//
//fixedRate = 60 * 60 * 1000 → runs every 1 hour.
//
//Can also use cron expressions for more flexibility.
//
//No user interaction is needed; backend does it automatically
@EnableScheduling
@SpringBootApplication
public class SmarttodoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmarttodoApplication.class, args);
	}

}
