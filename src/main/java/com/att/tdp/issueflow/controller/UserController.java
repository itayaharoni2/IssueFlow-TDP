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
public class UserController {

    private final UserService userService;
    private final com.att.tdp.issueflow.service.CommentMentionService commentMentionService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PostMapping("/update/{userId}")
    public ResponseEntity<Void> updateUser(@PathVariable Long userId, @RequestBody UpdateUserRequest request) {
        userService.updateUser(userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{userId}/mentions")
    public ResponseEntity<com.att.tdp.issueflow.dto.user.MentionsResponse> getMentions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(commentMentionService.getMentions(userId, page, pageSize));
    }
}
