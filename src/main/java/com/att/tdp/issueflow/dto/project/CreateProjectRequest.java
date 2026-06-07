package com.att.tdp.issueflow.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing the payload to create a new project.
 * It validates that a project name and an owner ID are provided by the client.
 */
public class CreateProjectRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "OwnerId is required")
    private Long ownerId;
}
