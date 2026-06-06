package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.project.CreateProjectRequest;
import com.att.tdp.issueflow.dto.project.ProjectResponse;
import com.att.tdp.issueflow.dto.project.UpdateProjectRequest;
import com.att.tdp.issueflow.dto.project.WorkloadResponse;
import com.att.tdp.issueflow.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllActiveProjects() {
        return ResponseEntity.ok(projectService.getActiveProjects());
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProjectResponse>> getDeletedProjects() {
        return ResponseEntity.ok(projectService.getDeletedProjects());
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.ok(projectService.createProject(request));
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<Void> updateProject(@PathVariable Long projectId, @RequestBody UpdateProjectRequest request) {
        projectService.updateProject(projectId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> softDeleteProject(@PathVariable Long projectId) {
        projectService.softDeleteProject(projectId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{projectId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restoreProject(@PathVariable Long projectId) {
        projectService.restoreProject(projectId);
        return ResponseEntity.ok().build();
    }
    
    // Workload will be implemented after Ticket queries are available.
    // For now we just create an empty stub so it compiles.
    @GetMapping("/{projectId}/workload")
    public ResponseEntity<List<WorkloadResponse>> getWorkload(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getWorkload(projectId));
    }
}
