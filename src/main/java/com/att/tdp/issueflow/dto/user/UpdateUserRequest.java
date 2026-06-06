package com.att.tdp.issueflow.dto.user;

import com.att.tdp.issueflow.entity.enums.Role;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object for update user request.
 */
public class UpdateUserRequest {
    private String fullName;
    private Role role;
}
