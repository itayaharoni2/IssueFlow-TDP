package com.att.tdp.issueflow.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
/**
 * Role: Data Transfer Object representing the client payload for a login request.
 * It contains the user's provided credentials (username and password) which are validated for authentication.
 */
public class LoginRequest {

    @NotBlank(message = "username is required")
    @Size(max = 25, message = "Username must not exceed 25 characters")
    private String username;

    @NotBlank(message = "password is required")
    @Size(max = 128, message = "Password must not exceed 128 characters")
    private String password;
}
