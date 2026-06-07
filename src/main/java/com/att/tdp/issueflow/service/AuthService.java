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
 * Role: Service layer managing user authentication and session states.
 * It integrates with Spring Security to validate credentials, generates and invalidates JWT tokens, and retrieves the current authenticated user's profile.
 */
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final JwtAuthFilter jwtAuthFilter;
    private final UserRepository userRepository;

    /**
     * Authenticates a user's credentials against the database and issues a signed JWT upon success.
     * Delegates validation to Spring Security's AuthenticationManager.
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
     * Processes a user logout by invalidating the provided JWT, adding it to a blacklist to prevent further use.
     */
    public void logout(String rawToken) {
        jwtAuthFilter.blacklistToken(rawToken);
    }

    /**
     * Fetches the profile details of the user who is currently authenticated in the Spring Security context.
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
