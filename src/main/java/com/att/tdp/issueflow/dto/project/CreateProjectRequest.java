package com.att.tdp.issueflow.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing the payload to create a new project.
 * It validates that a project name and an owner ID are provided by the client.
 */
public class CreateProjectRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 200, message = "Project name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "OwnerId is required")
    private Long ownerId;
}
