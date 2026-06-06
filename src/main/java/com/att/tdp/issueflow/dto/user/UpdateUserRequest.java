package com.att.tdp.issueflow.dto.user;

import com.att.tdp.issueflow.entity.enums.Role;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;
    private Role role;
}
