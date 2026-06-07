package com.att.tdp.issueflow.dto.project;

import com.att.tdp.issueflow.entity.Project;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing a project returned to the client.
 * It encapsulates the basic project details, including its name, description, and the ID of its owner.
 */
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;

    /**
     * Constructs a ProjectResponse object from a given Project entity.
     */
    public ProjectResponse(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.ownerId = project.getOwner().getId();
    }
}
