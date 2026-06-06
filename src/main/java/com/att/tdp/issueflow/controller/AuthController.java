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
public class AuthController {

    private final AuthService authService;

    /**
     * POST /auth/login
     * Public endpoint — no JWT required.
     * Returns: { accessToken, tokenType, expiresIn }
     */
    @PostMapping("/login")
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
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
}
