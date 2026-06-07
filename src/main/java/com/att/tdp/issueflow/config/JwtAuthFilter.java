package com.att.tdp.issueflow.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
/**
 * Role: Acts as a security filter that intercepts HTTP requests to authenticate
 * users via JWT.
 * It works by extracting the JWT from the Authorization header, validating its
 * signature and expiration,
 * and setting the authenticated UserDetails in the Spring SecurityContext.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    /**
     * In-memory deny-list for logout.
     * Token string → blacklisted. Lost on restart.
     */
    private final Set<String> tokenDenyList = ConcurrentHashMap.newKeySet();

    /**
     * Add a raw JWT string to the deny-list (called from AuthService on logout).
     */
    public void blacklistToken(String token) {
        tokenDenyList.add(token);
    }

    /**
     * Clears all tokens from the in-memory deny-list.
     */
    public void clearDenyList() {
        tokenDenyList.clear();
    }

    @Override
    /**
     * Intercepts the request to extract and validate the JWT token.
     * If valid, it authenticates the user in the security context.
     */
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null
                && !tokenDenyList.contains(token)
                && jwtProvider.isTokenValid(token)) {

            String username = jwtProvider.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw JWT token string from the HTTP Authorization header.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
