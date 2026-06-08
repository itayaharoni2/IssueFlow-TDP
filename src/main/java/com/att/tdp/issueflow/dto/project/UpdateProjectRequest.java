package com.att.tdp.issueflow.dto.project;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing the payload to update an existing project.
 * It contains optional fields like name and description that the client wishes to modify.
 */
public class UpdateProjectRequest {
    @Size(min = 1, max = 200, message = "Project name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
