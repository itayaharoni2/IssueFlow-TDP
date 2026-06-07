package com.att.tdp.issueflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
/**
 * Role: The main entry point for the IssueFlow Spring Boot application.
 * It bootstraps the application context, configures beans, and starts the embedded web server.
 */
public class IssueFlowApplication {

	/**
	 * Main method that launches the Spring Boot application.
	 */
	public static void main(String[] args) {
		SpringApplication.run(IssueFlowApplication.class, args);
	}

}
