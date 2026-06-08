package com.att.tdp.issueflow.dto.user;

import com.att.tdp.issueflow.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing the payload to create a new user.
 * It enforces validation rules for required fields like username, email, full name, and role.
 */
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 25, message = "Username must be between 3 and 25 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String email;

    @NotBlank(message = "FullName is required")
    @Size(min = 1, max = 50, message = "Full name must not exceed 50 characters")
    private String fullName;

    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password must contain at least one letter and one number")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;
}
