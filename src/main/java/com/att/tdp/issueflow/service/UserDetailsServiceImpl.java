package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.Role;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security bridge: loads a User entity by username and wraps it
 * in a Spring Security UserDetails with a ROLE_<role> granted authority.
 */
@Service
@RequiredArgsConstructor
/**
 * Role: Implementation of Spring Security's UserDetailsService interface.
 * It bridges the application's User entity with Spring Security's authentication mechanisms.
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    /**
     * Looks up a user by username and constructs a Spring Security UserDetails object populated with granted authorities.
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));

        String roleName = "ROLE_" + user.getRole().name();  // e.g. ROLE_ADMIN, ROLE_DEVELOPER

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(roleName))
        );
    }
}
