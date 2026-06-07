package com.att.tdp.issueflow.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
/**
 * Role: Data Transfer Object representing the response sent after successful
 * authentication.
 * It encapsulates the newly generated JWT token, its type, and its expiration
 * time to be used by the client.
 */
public class LoginResponse {

    // The signed JWT string.
    private String accessToken;

    // Always "Bearer".
    private String tokenType;

    // Token lifetime in seconds (config value divided by 1000).
    private long expiresIn;
}
