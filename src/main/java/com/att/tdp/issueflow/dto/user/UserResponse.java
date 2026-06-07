package com.att.tdp.issueflow.dto.user;

import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.Role;
import lombok.Getter;

@Getter
/**
 * Role: Data Transfer Object representing a user's public profile returned to the client.
 * It encapsulates the user's basic identification and contact details while excluding sensitive information like passwords.
 */
public class UserResponse {

    private final Long id;
    private final String username;
    private final String email;
    private final String fullName;
    private final Role role;

    /**
     * Constructs a UserResponse object from a given User entity.
     */
    public UserResponse(User user) {
        this.id       = user.getId();
        this.username = user.getUsername();
        this.email    = user.getEmail();
        this.fullName = user.getFullName();
        this.role     = user.getRole();
    }
}
