package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.project.CreateProjectRequest;
import com.att.tdp.issueflow.dto.project.ProjectResponse;
import com.att.tdp.issueflow.dto.project.UpdateProjectRequest;
import com.att.tdp.issueflow.dto.project.WorkloadResponse;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.ProjectRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Role: Service layer managing top-level projects within the system.
 * It handles standard CRUD operations along with soft-delete mechanics and calculating developer workloads per project.
 */
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final com.att.tdp.issueflow.repository.TicketRepository ticketRepository;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    @Transactional(readOnly = true)
    /**
     * Fetches a list of all current projects that have not been soft-deleted.
     */
    public List<ProjectResponse> getActiveProjects() {
        return projectRepository.findAllByDeletedAtIsNull().stream()
                .map(ProjectResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    /**
     * Fetches a list of all projects that have been soft-deleted (archived).
     */
    public List<ProjectResponse> getDeletedProjects() {
        return projectRepository.findAllByDeletedAtIsNotNull().stream()
                .map(ProjectResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves the specific details of an active project by its unique identifier.
     */
    public ProjectResponse getProjectById(Long projectId) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        return new ProjectResponse(project);
    }

    @Transactional
    /**
     * Validates input and provisions a new project, establishing the owner and generating a creation audit log.
     */
    public ProjectResponse createProject(CreateProjectRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner user not found"));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(owner);

        Project saved = projectRepository.save(project);
        
        Long currentUserId = getCurrentUserId();
        auditLogService.log(AuditAction.CREATE, "Project", saved.getId(), currentUserId, "USER");
        
        return new ProjectResponse(saved);
    }

    @Transactional
    /**
     * Applies partial updates to a project's details, persisting only fields that were actually modified.
     */
    public void updateProject(Long projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        boolean updated = false;
        if (request.getName() != null) {
            project.setName(request.getName());
            updated = true;
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
            updated = true;
        }

        if (updated) {
            projectRepository.save(project);
            auditLogService.log(AuditAction.UPDATE, "Project", project.getId(), getCurrentUserId(), "USER");
        }
    }

    @Transactional
    /**
     * Soft-deletes a project by stamping a deletion timestamp, hiding it from active views without destroying data.
     */
    public void softDeleteProject(Long projectId) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        project.setDeletedAt(LocalDateTime.now());
        projectRepository.save(project);
        
        auditLogService.log(AuditAction.DELETE, "Project", project.getId(), getCurrentUserId(), "USER");
    }

    @Transactional
    /**
     * Restores a soft-deleted project by clearing its deletion timestamp, making it active again.
     */
    public void restoreProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (project.getDeletedAt() != null) {
            project.setDeletedAt(null);
            projectRepository.save(project);
            auditLogService.log(AuditAction.RESTORE, "Project", project.getId(), getCurrentUserId(), "USER");
        }
    }

    @Transactional(readOnly = true)
    /**
     * Calculates the active ticket load for all developers on a given project, sorted from least to most busy.
     */
    public List<WorkloadResponse> getWorkload(Long projectId) {
        projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        List<User> developers = userRepository.findAllByRole(com.att.tdp.issueflow.entity.enums.Role.DEVELOPER);
        
        return developers.stream()
                .map(user -> {
                    long count = ticketRepository.countByProjectIdAndAssigneeIdAndStatusNotAndDeletedAtIsNull(
                            projectId, user.getId(), com.att.tdp.issueflow.entity.enums.TicketStatus.DONE);
                    return new WorkloadResponse(user.getId(), user.getUsername(), count);
                })
                .sorted((w1, w2) -> {
                    int cmp = Long.compare(w1.getOpenTicketCount(), w2.getOpenTicketCount());
                    if (cmp == 0) {
                        User u1 = userRepository.findById(w1.getUserId()).get();
                        User u2 = userRepository.findById(w2.getUserId()).get();
                        return u1.getCreatedAt().compareTo(u2.getCreatedAt());
                    }
                    return cmp;
                })
                .collect(Collectors.toList());
    }

    /**
     * Helper method to extract the ID of the currently authenticated user from the security context.
     */
    private Long getCurrentUserId() {
        try {
            return authService.getCurrentUser().getId();
        } catch (Exception e) {
            return null; // For test cases without auth
        }
    }
}
