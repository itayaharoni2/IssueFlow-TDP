package com.att.tdp.issueflow.dto.user;

import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.Role;
import lombok.Getter;

@Getter
/**
 * Role: Data Transfer Object for user response.
 */
public class UserResponse {

    private final Long id;
    private final String username;
    private final String email;
    private final String fullName;
    private final Role role;

    public UserResponse(User user) {
        this.id       = user.getId();
        this.username = user.getUsername();
        this.email    = user.getEmail();
        this.fullName = user.getFullName();
        this.role     = user.getRole();
    }
}
