package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.config.JwtAuthFilter;
import com.att.tdp.issueflow.config.JwtProvider;
import com.att.tdp.issueflow.dto.auth.LoginRequest;
import com.att.tdp.issueflow.dto.auth.LoginResponse;
import com.att.tdp.issueflow.dto.user.UserResponse;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/**
 * Role: Handles business logic and operations for auth.
 */
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final JwtAuthFilter jwtAuthFilter;
    private final UserRepository userRepository;

    /**
     * Authenticate username + password and return a signed JWT.
     * Spring Security's AuthenticationManager validates credentials and throws
     * BadCredentialsException on failure (mapped to 401 by Spring).
     */
    /**
     * Executes the login operation.
     */
    public LoginResponse login(LoginRequest request) {
        // Delegates to DaoAuthenticationProvider → UserDetailsServiceImpl → DB
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        String token = jwtProvider.generateToken(request.getUsername());

        return new LoginResponse(token, "Bearer", jwtProvider.getExpirationSeconds());
    }

    /**
     * Invalidate the current request's JWT by adding it to the deny-list.
     *
     * @param rawToken the raw JWT string extracted from the Authorization header
     *                 (without the "Bearer " prefix — the controller passes it trimmed)
     */
    /**
     * Executes the logout operation.
     */
    public void logout(String rawToken) {
        jwtAuthFilter.blacklistToken(rawToken);
    }

    /**
     * Return the authenticated user's profile.
     * Reads the username from the SecurityContext (set by JwtAuthFilter).
     */
    /**
     * Retrieves current user.
     */
    public UserResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found: " + username));

        return new UserResponse(user);
    }
}
