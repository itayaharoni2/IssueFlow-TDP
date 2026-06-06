package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.BaseIntegrationTest;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Service-level tests for auto-escalation.
 * Tests call EscalationService.escalateOverdueTickets() directly to avoid
 * scheduler timing issues.
 */
class EscalationServiceTest extends BaseIntegrationTest {

    @Autowired
    private EscalationService escalationService;

    private User admin;
    private Project project;

    private void setup() {
        admin = createAdmin();
        project = createProject(admin, "P1");
    }

    @Test
    void overdueTicket_lowPriority_becomesMediam() {
        setup();
        Ticket ticket = createOverdueTicket(project, admin, TicketStatus.TODO, TicketPriority.LOW);

        escalationService.escalateOverdueTickets();

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getPriority()).isEqualTo(TicketPriority.MEDIUM);
        assertThat(updated.isOverdue()).isFalse();
    }

    @Test
    void overdueTicket_mediumPriority_becomesHigh() {
        setup();
        Ticket ticket = createOverdueTicket(project, admin, TicketStatus.TODO, TicketPriority.MEDIUM);

        escalationService.escalateOverdueTickets();

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getPriority()).isEqualTo(TicketPriority.HIGH);
    }

    @Test
    void overdueTicket_highPriority_becomesCritical() {
        setup();
        Ticket ticket = createOverdueTicket(project, admin, TicketStatus.TODO, TicketPriority.HIGH);

        escalationService.escalateOverdueTickets();

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getPriority()).isEqualTo(TicketPriority.CRITICAL);
    }

    @Test
    void overdueTicket_criticalPriority_setsIsOverdue() {
        setup();
        Ticket ticket = createOverdueTicket(project, admin, TicketStatus.TODO, TicketPriority.CRITICAL);

        escalationService.escalateOverdueTickets();

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getPriority()).isEqualTo(TicketPriority.CRITICAL); // stays CRITICAL
        assertThat(updated.isOverdue()).isTrue();
    }

    @Test
    void doneTicket_notEscalated() {
        setup();
        Ticket ticket = createOverdueTicket(project, admin, TicketStatus.DONE, TicketPriority.LOW);

        escalationService.escalateOverdueTickets();

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getPriority()).isEqualTo(TicketPriority.LOW); // unchanged
    }

    @Test
    void ticketWithNoDueDate_notEscalated() {
        setup();
        Ticket ticket = createTicket(project, admin, null, TicketStatus.TODO, TicketPriority.LOW);
        // No dueDate set

        escalationService.escalateOverdueTickets();

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getPriority()).isEqualTo(TicketPriority.LOW); // unchanged
    }

    @Test
    void ticketWithFutureDueDate_notEscalated() {
        setup();
        Ticket ticket = new Ticket();
        ticket.setTitle("Future ticket");
        ticket.setStatus(TicketStatus.TODO);
        ticket.setPriority(TicketPriority.LOW);
        ticket.setType(com.att.tdp.issueflow.entity.enums.TicketType.BUG);
        ticket.setProject(project);
        ticket.setReporter(admin);
        ticket.setDueDate(java.time.OffsetDateTime.now().plusDays(7)); // future
        ticketRepository.save(ticket);

        escalationService.escalateOverdueTickets();

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getPriority()).isEqualTo(TicketPriority.LOW); // unchanged
    }

    @Test
    void softDeletedTicket_notEscalated() {
        setup();
        Ticket ticket = createOverdueTicket(project, admin, TicketStatus.TODO, TicketPriority.LOW);
        ticket.setDeletedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        escalationService.escalateOverdueTickets();

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getPriority()).isEqualTo(TicketPriority.LOW); // unchanged
    }

    @Test
    void escalation_doesNotChangeStatus() {
        setup();
        Ticket ticket = createOverdueTicket(project, admin, TicketStatus.IN_PROGRESS, TicketPriority.LOW);

        escalationService.escalateOverdueTickets();

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS); // status unchanged
        assertThat(updated.getPriority()).isEqualTo(TicketPriority.MEDIUM);  // priority escalated
    }

    @Test
    void escalation_writesAuditLog() {
        setup();
        createOverdueTicket(project, admin, TicketStatus.TODO, TicketPriority.LOW);

        long logsBefore = auditLogRepository.count();
        escalationService.escalateOverdueTickets();
        long logsAfter = auditLogRepository.count();

        // An ESCALATE audit log should have been written
        assertThat(logsAfter).isGreaterThan(logsBefore);
        assertThat(auditLogRepository.findAll().stream()
                .anyMatch(l -> AuditAction.ESCALATE.equals(l.getAction()) && "SYSTEM".equals(l.getActor())))
                .isTrue();
    }

    @Test
    void criticalEscalation_idempotent_runsMultipleTimes() {
        setup();
        Ticket ticket = createOverdueTicket(project, admin, TicketStatus.TODO, TicketPriority.CRITICAL);

        escalationService.escalateOverdueTickets();
        escalationService.escalateOverdueTickets(); // run again

        Ticket updated = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertThat(updated.getPriority()).isEqualTo(TicketPriority.CRITICAL); // still CRITICAL
        assertThat(updated.isOverdue()).isTrue();
    }
}
