package com.att.tdp.issueflow.dto.project;

import lombok.Data;

@Data
/**
 * Role: Data Transfer Object for update project request.
 */
public class UpdateProjectRequest {
    private String name;
    private String description;
}
