package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.user.CreateUserRequest;
import com.att.tdp.issueflow.dto.user.UpdateUserRequest;
import com.att.tdp.issueflow.dto.user.UserResponse;
import com.att.tdp.issueflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
/**
 * Role: Provides REST API endpoints for managing system users.
 * It allows administrators to create, update, delete, and query user profiles, as well as fetch user mentions.
 */
public class UserController {

    private final UserService userService;
    private final com.att.tdp.issueflow.service.CommentMentionService commentMentionService;

    @GetMapping
    /**
     * Retrieves a list of all active users in the system.
     */
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    /**
     * Retrieves the details of a specific user by their ID.
     */
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PostMapping
    /**
     * Creates a new user profile in the system.
     */
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PostMapping("/update/{userId}")
    /**
     * Updates the profile information of an existing user.
     */
    public ResponseEntity<Void> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest request) {
        userService.updateUser(userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    /**
     * Deletes a specific user from the system.
     */
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
    /**
     * Retrieves a paginated list of comments where the specified user was mentioned.
     */
    @GetMapping("/{userId}/mentions")
    public ResponseEntity<com.att.tdp.issueflow.dto.user.MentionsResponse> getMentions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(commentMentionService.getMentions(userId, page, pageSize));
    }
}
