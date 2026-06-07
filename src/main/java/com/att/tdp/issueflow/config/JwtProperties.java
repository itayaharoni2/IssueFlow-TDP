package com.att.tdp.issueflow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
/**
 * Role: Maps custom JWT properties from the application configuration
 * (application.yaml).
 * It works by binding properties with the "jwt" prefix to the fields of this
 * class,
 * making them injectable and accessible throughout the application without
 * using @Value annotations.
 */
public class JwtProperties {

    // The secret key used for signing JWTs.
    private String secret;

    // Token expiration time in milliseconds.
    private long expirationMs;
}
