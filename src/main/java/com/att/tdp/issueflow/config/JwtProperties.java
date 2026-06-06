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
 * Role: Maps custom JWT properties from application.yaml.
 * This class eliminates IDE warnings about "Unknown property 'jwt'".
 */
public class JwtProperties {

    /**
     * The secret key used for signing JWTs.
     */
    private String secret;

    /**
     * Token expiration time in milliseconds.
     */
    private long expirationMs;
}
