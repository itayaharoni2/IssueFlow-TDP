package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.ticket.CreateTicketRequest;
import com.att.tdp.issueflow.dto.ticket.TicketResponse;
import com.att.tdp.issueflow.dto.ticket.UpdateTicketRequest;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.ProjectRepository;
import com.att.tdp.issueflow.repository.TicketRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<TicketResponse> getActiveTickets(Long projectId) {
        return ticketRepository.findAllByProjectIdAndDeletedAtIsNull(projectId).stream()
                .map(TicketResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getDeletedTickets(Long projectId) {
        return ticketRepository.findAllByProjectIdAndDeletedAtIsNotNull(projectId).stream()
                .map(TicketResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        return new TicketResponse(ticket);
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User reporter = null;
        try {
            reporter = userRepository.findById(authService.getCurrentUser().getId()).orElseThrow();
        } catch (Exception e) {
            // Test fallback or if without auth
        }

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
        } 
        // Note: Phase 4 auto-assignment logic would go here if assignee is null

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setStatus(request.getStatus());
        ticket.setPriority(request.getPriority());
        ticket.setType(request.getType());
        ticket.setProject(project);
        ticket.setReporter(reporter);
        ticket.setAssignee(assignee);
        ticket.setDueDate(request.getDueDate());

        Ticket saved = ticketRepository.save(ticket);
        
        Long currentUserId = getCurrentUserId();
        auditLogService.log(AuditAction.CREATE, "Ticket", saved.getId(), currentUserId, "USER");

        return new TicketResponse(saved);
    }

    @Transactional
    public void updateTicket(Long ticketId, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        boolean updated = false;

        // Phase 4 lifecycle validation would go here

        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
            updated = true;
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
            updated = true;
        }
        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
            updated = true;
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
            // "Manual priority PATCH resets isOverdue to false"
            ticket.setOverdue(false);
            updated = true;
        }
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
            ticket.setAssignee(assignee);
            updated = true;
        }
        if (request.getDueDate() != null) {
            ticket.setDueDate(request.getDueDate());
            updated = true;
        }

        if (updated) {
            ticketRepository.save(ticket);
            auditLogService.log(AuditAction.UPDATE, "Ticket", ticket.getId(), getCurrentUserId(), "USER");
        }
    }

    @Transactional
    public void softDeleteTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ticket.setDeletedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        
        auditLogService.log(AuditAction.DELETE, "Ticket", ticket.getId(), getCurrentUserId(), "USER");
    }

    @Transactional
    public void restoreTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (ticket.getDeletedAt() != null) {
            ticket.setDeletedAt(null);
            ticketRepository.save(ticket);
            auditLogService.log(AuditAction.RESTORE, "Ticket", ticket.getId(), getCurrentUserId(), "USER");
        }
    }

    private Long getCurrentUserId() {
        try {
            return authService.getCurrentUser().getId();
        } catch (Exception e) {
            return null; // For test cases without auth
        }
    }
}
