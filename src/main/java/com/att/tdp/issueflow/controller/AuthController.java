package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.auth.LoginRequest;
import com.att.tdp.issueflow.dto.auth.LoginResponse;
import com.att.tdp.issueflow.dto.user.UserResponse;
import com.att.tdp.issueflow.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
/**
 * Role: Provides REST API endpoints for authentication and user sessions.
 * It handles login requests to issue JWTs, logout requests to invalidate them, and fetching the current authenticated user's profile.
 */
public class AuthController {

    private final AuthService authService;

    /**
     * POST /auth/login
     * Public endpoint — no JWT required.
     * Returns: { accessToken, tokenType, expiresIn }
     */
    @PostMapping("/login")
    /**
     * Authenticates user credentials and returns a signed JWT token upon success.
     */
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/logout
     * Requires: valid JWT in Authorization header.
     * Adds the token to the in-memory deny-list so it cannot be reused.
     */
    @PostMapping("/logout")
    /**
     * Invalidates the current user's session by adding their JWT token to the deny-list.
     */
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * GET /auth/me
     * Requires: valid JWT.
     * Returns the currently authenticated user's profile.
     */
    @GetMapping("/me")
    /**
     * Retrieves the profile information of the currently authenticated user.
     */
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
}
