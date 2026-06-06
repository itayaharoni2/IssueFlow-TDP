package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.ticket.DependencyResponse;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.TicketDependency;
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
public class DependencyService {

    private final TicketDependencyRepository dependencyRepository;
    private final TicketRepository ticketRepository;

    @Transactional
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
    }

    @Transactional(readOnly = true)
    public List<DependencyResponse> getDependencies(Long ticketId) {
        ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        return dependencyRepository.findAllByTicketId(ticketId).stream()
                .map(dep -> new DependencyResponse(dep.getBlockedBy()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeDependency(Long ticketId, Long blockedById) {
        ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        List<TicketDependency> dependencies = dependencyRepository.findAllByTicketId(ticketId);
        TicketDependency dependencyToRemove = dependencies.stream()
                .filter(d -> d.getBlockedBy().getId().equals(blockedById))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Dependency not found"));

        dependencyRepository.delete(dependencyToRemove);
    }
}
