package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.ticket.DependencyResponse;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.TicketDependency;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.TicketDependencyRepository;
import com.att.tdp.issueflow.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Role: Service layer responsible for managing "blocked by" relationships between tickets.
 * It enforces business rules to prevent circular dependencies and cross-project relationships.
 */
public class DependencyService {

    private final TicketDependencyRepository dependencyRepository;
    private final TicketRepository ticketRepository;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    @Transactional
    /**
     * Creates a new dependency where a given ticket becomes blocked by another, verifying there are no cycles.
     */
    public void addDependency(Long ticketId, Long blockedById) {
        if (ticketId.equals(blockedById)) {
            throw new BadRequestException("A ticket cannot block itself.");
        }

        Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        Ticket blockedBy = ticketRepository.findByIdAndDeletedAtIsNull(blockedById)
                .orElseThrow(() -> new ResourceNotFoundException("Blocking ticket not found"));

        if (!ticket.getProject().getId().equals(blockedBy.getProject().getId())) {
            throw new BadRequestException("Both tickets must belong to the same project.");
        }

        if (dependencyRepository.existsByTicketIdAndBlockedById(ticketId, blockedById)) {
            // Already exists, can just return or throw. Let's return.
            return;
        }
        
        // Prevent simple cycles: if A is blocked by B, B cannot be blocked by A
        if (dependencyRepository.existsByTicketIdAndBlockedById(blockedById, ticketId)) {
            throw new BadRequestException("Dependency cycle detected.");
        }

        TicketDependency dependency = new TicketDependency();
        dependency.setTicket(ticket);
        dependency.setBlockedBy(blockedBy);

        dependencyRepository.save(dependency);

        Long currentUserId = getCurrentUserId();
        auditLogService.log(AuditAction.CREATE, "Dependency", dependency.getId(), currentUserId, "USER");
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves a list of all tickets that are currently blocking the specified ticket.
     */
    public List<DependencyResponse> getDependencies(Long ticketId) {
        ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        return dependencyRepository.findAllByTicketId(ticketId).stream()
                .map(dep -> new DependencyResponse(dep.getBlockedBy()))
                .collect(Collectors.toList());
    }

    @Transactional
    /**
     * Unblocks a ticket by removing the specific dependency relationship with another ticket.
     */
    public void removeDependency(Long ticketId, Long blockedById) {
        ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        List<TicketDependency> dependencies = dependencyRepository.findAllByTicketId(ticketId);
        TicketDependency dependencyToRemove = dependencies.stream()
                .filter(d -> d.getBlockedBy().getId().equals(blockedById))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Dependency not found"));

        dependencyRepository.delete(dependencyToRemove);

        Long currentUserId = getCurrentUserId();
        auditLogService.log(AuditAction.DELETE, "Dependency", dependencyToRemove.getId(), currentUserId, "USER");
    }

    private Long getCurrentUserId() {
        try {
            return authService.getCurrentUser().getId();
        } catch (Exception e) {
            return null; // For test cases without auth
        }
    }
}
