package com.att.tdp.issueflow.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object for create project request.
 */
public class CreateProjectRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "OwnerId is required")
    private Long ownerId;
}
