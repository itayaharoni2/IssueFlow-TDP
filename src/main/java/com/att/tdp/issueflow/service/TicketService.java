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
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import com.att.tdp.issueflow.dto.ticket.ImportResultResponse;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketType;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Role: Handles business logic and operations for ticket.
 */
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final com.att.tdp.issueflow.repository.TicketDependencyRepository ticketDependencyRepository;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    @Transactional(readOnly = true)
    /**
     * Retrieves active tickets.
     */
    public List<TicketResponse> getActiveTickets(Long projectId) {
        return ticketRepository.findAllByProjectIdAndDeletedAtIsNull(projectId).stream()
                .map(TicketResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves deleted tickets.
     */
    public List<TicketResponse> getDeletedTickets(Long projectId) {
        return ticketRepository.findAllByProjectIdAndDeletedAtIsNotNull(projectId).stream()
                .map(TicketResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves ticket by id.
     */
    public TicketResponse getTicketById(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        return new TicketResponse(ticket);
    }

    @Transactional
    /**
     * Creates a new ticket.
     */
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
        } else {
            // Auto-assignment logic
            List<User> developers = userRepository.findAllByRole(com.att.tdp.issueflow.entity.enums.Role.DEVELOPER);
            if (!developers.isEmpty()) {
                assignee = developers.stream()
                        .min((u1, u2) -> {
                            long w1 = ticketRepository.countByProjectIdAndAssigneeIdAndStatusNotAndDeletedAtIsNull(project.getId(), u1.getId(), com.att.tdp.issueflow.entity.enums.TicketStatus.DONE);
                            long w2 = ticketRepository.countByProjectIdAndAssigneeIdAndStatusNotAndDeletedAtIsNull(project.getId(), u2.getId(), com.att.tdp.issueflow.entity.enums.TicketStatus.DONE);
                            if (w1 != w2) {
                                return Long.compare(w1, w2);
                            }
                            return u1.getCreatedAt().compareTo(u2.getCreatedAt());
                        })
                        .orElse(null);
            }
        }

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

        if (request.getAssigneeId() == null && assignee != null) {
            auditLogService.log(AuditAction.AUTO_ASSIGN, "Ticket", saved.getId(), null, "SYSTEM");
        }

        return new TicketResponse(saved);
    }

    @Transactional
    /**
     * Updates an existing ticket.
     */
    public void updateTicket(Long ticketId, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (com.att.tdp.issueflow.entity.enums.TicketStatus.DONE.equals(ticket.getStatus())) {
            throw new com.att.tdp.issueflow.exception.BadRequestException("Cannot update a ticket that is already DONE.");
        }

        boolean updated = false;

        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
            updated = true;
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
            updated = true;
        }
        if (request.getStatus() != null && !request.getStatus().equals(ticket.getStatus())) {
            validateStatusTransition(ticket.getStatus(), request.getStatus(), ticketId);
            ticket.setStatus(request.getStatus());
            updated = true;
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
            ticket.setOverdue(false); // Manual priority PATCH resets isOverdue to false
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
    /**
     * Executes the soft delete ticket operation.
     */
    public void softDeleteTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ticket.setDeletedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        
        auditLogService.log(AuditAction.DELETE, "Ticket", ticket.getId(), getCurrentUserId(), "USER");
    }

    @Transactional
    /**
     * Executes the restore ticket operation.
     */
    public void restoreTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (ticket.getDeletedAt() != null) {
            ticket.setDeletedAt(null);
            ticketRepository.save(ticket);
            auditLogService.log(AuditAction.RESTORE, "Ticket", ticket.getId(), getCurrentUserId(), "USER");
        }
    }

    /**
     * Executes the export tickets to csv operation.
     */
    public void exportTicketsToCsv(Long projectId, Writer writer) {
        List<Ticket> tickets = ticketRepository.findAllByProjectIdAndDeletedAtIsNull(projectId);
        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader("id", "title", "description", "status", "priority", "type", "assigneeId").build())) {
            for (Ticket ticket : tickets) {
                csvPrinter.printRecord(
                        ticket.getId(),
                        ticket.getTitle(),
                        ticket.getDescription(),
                        ticket.getStatus(),
                        ticket.getPriority(),
                        ticket.getType(),
                        ticket.getAssignee() != null ? ticket.getAssignee().getId() : null
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to write CSV data", e);
        }
    }

    @Transactional
    /**
     * Executes the import tickets from csv operation.
     */
    public ImportResultResponse importTicketsFromCsv(Long projectId, MultipartFile file) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        int created = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true).build())) {

            for (CSVRecord csvRecord : csvParser) {
                try {
                    Ticket ticket = new Ticket();
                    ticket.setProject(project);
                    ticket.setTitle(csvRecord.get("title"));
                    ticket.setDescription(csvRecord.get("description"));
                    ticket.setStatus(TicketStatus.valueOf(csvRecord.get("status").toUpperCase()));
                    ticket.setPriority(TicketPriority.valueOf(csvRecord.get("priority").toUpperCase()));
                    ticket.setType(TicketType.valueOf(csvRecord.get("type").toUpperCase()));

                    String assigneeIdStr = csvRecord.isSet("assigneeId") ? csvRecord.get("assigneeId") : null;
                    if (assigneeIdStr != null && !assigneeIdStr.isEmpty()) {
                        Long assigneeId = Long.parseLong(assigneeIdStr);
                        User assignee = userRepository.findById(assigneeId)
                                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found for ID: " + assigneeId));
                        ticket.setAssignee(assignee);
                    }

                    // For reporter, if not provided, we can set current user or null. Rules say "assigneeId", no reporter specified for CSV.
                    User reporter = null;
                    try {
                        reporter = userRepository.findById(authService.getCurrentUser().getId()).orElse(null);
                    } catch (Exception ignored) {}
                    ticket.setReporter(reporter);

                    Ticket saved = ticketRepository.save(ticket);
                    auditLogService.log(AuditAction.CREATE, "Ticket", saved.getId(), getCurrentUserId(), "USER");
                    created++;
                } catch (Exception e) {
                    failed++;
                    errors.add("Row " + csvRecord.getRecordNumber() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file", e);
        }

        return new ImportResultResponse(created, failed, errors);
    }

    /**
     * Retrieves current user id.
     */
    private Long getCurrentUserId() {
        try {
            return authService.getCurrentUser().getId();
        } catch (Exception e) {
            return null; // For test cases without auth
        }
    }

    /**
     * Executes the validate status transition operation.
     */
    private void validateStatusTransition(com.att.tdp.issueflow.entity.enums.TicketStatus current, com.att.tdp.issueflow.entity.enums.TicketStatus next, Long ticketId) {
        if (current == com.att.tdp.issueflow.entity.enums.TicketStatus.TODO && next != com.att.tdp.issueflow.entity.enums.TicketStatus.IN_PROGRESS) {
            throw new com.att.tdp.issueflow.exception.BadRequestException("Invalid status transition from TODO. Next status must be IN_PROGRESS.");
        }
        if (current == com.att.tdp.issueflow.entity.enums.TicketStatus.IN_PROGRESS && next != com.att.tdp.issueflow.entity.enums.TicketStatus.IN_REVIEW) {
            throw new com.att.tdp.issueflow.exception.BadRequestException("Invalid status transition from IN_PROGRESS. Next status must be IN_REVIEW.");
        }
        if (current == com.att.tdp.issueflow.entity.enums.TicketStatus.IN_REVIEW && next != com.att.tdp.issueflow.entity.enums.TicketStatus.DONE) {
            throw new com.att.tdp.issueflow.exception.BadRequestException("Invalid status transition from IN_REVIEW. Next status must be DONE.");
        }

        if (next == com.att.tdp.issueflow.entity.enums.TicketStatus.DONE) {
            boolean hasUnresolvedBlockers = ticketDependencyRepository.findAllByTicketId(ticketId).stream()
                    .anyMatch(dep -> !com.att.tdp.issueflow.entity.enums.TicketStatus.DONE.equals(dep.getBlockedBy().getStatus()));
            if (hasUnresolvedBlockers) {
                throw new com.att.tdp.issueflow.exception.BadRequestException("Cannot transition to DONE: Ticket has unresolved blocking dependencies.");
            }
        }
    }
}
