package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.user.CreateUserRequest;
import com.att.tdp.issueflow.dto.user.UpdateUserRequest;
import com.att.tdp.issueflow.dto.user.UserResponse;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Role: Service layer responsible for managing user accounts.
 * It handles the creation of users, securely encoding passwords, updating roles/profiles, and managing user deletions.
 */
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    @Transactional(readOnly = true)
    /**
     * Retrieves a list of all registered users in the system.
     */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves the specific profile details for a user by their unique identifier.
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserResponse(user);
    }

    @Transactional
    /**
     * Registers a new user, ensuring username and email uniqueness, and securely hashing their password.
     */
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());

        String rawPassword = request.getPassword() != null && !request.getPassword().isEmpty()
                ? request.getPassword()
                : "secret";
        user.setPassword(passwordEncoder.encode(rawPassword));

        User saved = userRepository.save(user);
        auditLogService.log(AuditAction.CREATE, "User", saved.getId(), saved.getId(), "USER");
        return new UserResponse(saved);
    }

    @Transactional
    /**
     * Applies partial updates to a user's profile or security role.
     */
    public void updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean updated = false;
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
            updated = true;
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
            updated = true;
        }

        if (updated) {
            userRepository.save(user);
            User currentUser = null;
            try {
                currentUser = authService.getCurrentUser() != null ? userRepository.findById(authService.getCurrentUser().getId()).orElse(null) : null;
            } catch (Exception e) {
                // Ignore if called without auth context in theory
            }
            Long currentUserId = currentUser != null ? currentUser.getId() : user.getId();
            auditLogService.log(AuditAction.UPDATE, "User", user.getId(), currentUserId, "USER");
        }
    }

    @Transactional
    /**
     * Permanently deletes a user from the system and logs the action.
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        userRepository.delete(user);
        
        Long currentUserId = null;
        try {
            currentUserId = authService.getCurrentUser().getId();
        } catch (Exception e) {}
        
        auditLogService.log(AuditAction.DELETE, "User", userId, currentUserId, "USER");
    }
}
