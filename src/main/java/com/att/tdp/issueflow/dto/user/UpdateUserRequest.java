package com.att.tdp.issueflow.dto.user;

import com.att.tdp.issueflow.entity.enums.Role;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing the payload to update an existing user's profile.
 * It provides optional fields such as full name and role that a client might want to modify.
 */
public class UpdateUserRequest {
    private String fullName;
    private Role role;
}
