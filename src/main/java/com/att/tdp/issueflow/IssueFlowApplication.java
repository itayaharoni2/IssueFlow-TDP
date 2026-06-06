package com.att.tdp.issueflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
/**
 * Role: Represents the issue flow application entity or object.
 */
public class IssueFlowApplication {

	/**
	 * Executes the main operation.
	 */
	public static void main(String[] args) {
		SpringApplication.run(IssueFlowApplication.class, args);
	}

}
