package com.att.tdp.issueflow.dto.user;

import com.att.tdp.issueflow.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "FullName is required")
    private String fullName;

    @NotNull(message = "Role is required")
    private Role role;

    private String password;
}
