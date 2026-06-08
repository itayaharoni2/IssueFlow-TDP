package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.project.CreateProjectRequest;
import com.att.tdp.issueflow.dto.project.ProjectResponse;
import com.att.tdp.issueflow.dto.project.UpdateProjectRequest;
import com.att.tdp.issueflow.dto.project.WorkloadResponse;
import com.att.tdp.issueflow.dto.common.PaginatedResponse;
import com.att.tdp.issueflow.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
/**
 * Role: Provides REST API endpoints for managing projects.
 * It exposes operations for creating, updating, retrieving, and deleting
 * projects, along with querying project workloads.
 */
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    // Retrieves a list of all active projects in the system.
    public ResponseEntity<PaginatedResponse<ProjectResponse>> getProjects(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(projectService.getActiveProjects(PageRequest.of(page - 1, size)));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    // Retrieves a list of all soft-deleted projects.
    public ResponseEntity<PaginatedResponse<ProjectResponse>> getDeletedProjects(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(projectService.getDeletedProjects(PageRequest.of(page - 1, size)));
    }

    @GetMapping("/{projectId}")
    // Retrieves the details of a specific project by its ID.
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @PostMapping
    // Creates a new project with the provided details.
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.ok(projectService.createProject(request));
    }

    @PatchMapping("/{projectId}")
    // Updates the details of an existing project.
    public ResponseEntity<Void> updateProject(@PathVariable Long projectId, @RequestBody UpdateProjectRequest request) {
        projectService.updateProject(projectId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{projectId}")
    // Soft deletes a specific project, marking it as inactive without permanently
    // removing its data.
    public ResponseEntity<Void> softDeleteProject(@PathVariable Long projectId) {
        projectService.softDeleteProject(projectId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{projectId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    // Restores a soft-deleted project back to active status. Requires ADMIN
    // privileges.
    public ResponseEntity<Void> restoreProject(@PathVariable Long projectId) {
        projectService.restoreProject(projectId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{projectId}/workload")
    // Retrieves the current workload statistics for a specific project.
    public ResponseEntity<List<WorkloadResponse>> getWorkload(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getWorkload(projectId));
    }
}
