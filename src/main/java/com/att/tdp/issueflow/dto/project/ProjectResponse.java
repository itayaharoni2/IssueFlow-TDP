package com.att.tdp.issueflow.dto.project;

import com.att.tdp.issueflow.entity.Project;
import lombok.Data;

@Data
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;

    public ProjectResponse(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.ownerId = project.getOwner().getId();
    }
}
