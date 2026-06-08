package com.att.tdp.issueflow.dto.user;

import com.att.tdp.issueflow.entity.enums.Role;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing the payload to update an existing user's profile.
 * It provides optional fields such as full name and role that a client might want to modify.
 */
public class UpdateUserRequest {
    @Size(min = 1, max = 50, message = "Full name must not exceed 50 characters")
    private String fullName;
    private Role role;
}
