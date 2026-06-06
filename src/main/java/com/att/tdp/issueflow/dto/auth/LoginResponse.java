package com.att.tdp.issueflow.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    /** The signed JWT string. */
    private String accessToken;

    /** Always "Bearer". */
    private String tokenType;

    /** Token lifetime in seconds (config value divided by 1000). */
    private long expiresIn;
}
