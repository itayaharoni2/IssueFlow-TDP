package com.att.tdp.issueflow.dto.project;

import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing the payload to update an existing project.
 * It contains optional fields like name and description that the client wishes to modify.
 */
public class UpdateProjectRequest {
    private String name;
    private String description;
}
