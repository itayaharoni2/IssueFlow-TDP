package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import com.att.tdp.issueflow.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Role: Handles business logic and operations for escalation.
 */
public class EscalationService {

    private final TicketRepository ticketRepository;
    private final AuditLogService auditLogService;

    @Transactional
    /**
     * Executes the escalate overdue tickets operation.
     */
    public void escalateOverdueTickets() {
        log.info("Running auto-escalation job...");
        List<Ticket> overdueTickets = ticketRepository.findByDueDateBeforeAndStatusNotAndDeletedAtIsNull(
                OffsetDateTime.now(), TicketStatus.DONE);

        for (Ticket ticket : overdueTickets) {
            boolean changed = false;

            switch (ticket.getPriority()) {
                case LOW:
                    ticket.setPriority(TicketPriority.MEDIUM);
                    changed = true;
                    break;
                case MEDIUM:
                    ticket.setPriority(TicketPriority.HIGH);
                    changed = true;
                    break;
                case HIGH:
                    ticket.setPriority(TicketPriority.CRITICAL);
                    changed = true;
                    break;
                case CRITICAL:
                    // idempotent for CRITICAL
                    if (!ticket.isOverdue()) {
                        ticket.setOverdue(true);
                        changed = true;
                    }
                    break;
            }

            if (changed) {
                ticketRepository.save(ticket);
                auditLogService.log(AuditAction.ESCALATE, "Ticket", ticket.getId(), null, "SYSTEM");
            }
        }
        
        log.info("Auto-escalation job finished. Escalated {} tickets.", overdueTickets.size());
    }
}
