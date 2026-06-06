package com.att.tdp.issueflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a "blockedBy" relationship between two tickets.
 * ticket (id=X) is blocked by blockedByTicket (id=Y).
 * Both tickets must belong to the same project.
 * Composite unique constraint prevents duplicate (ticketId, blockedById) pairs.
 */
@Entity
@Table(
    name = "ticket_dependencies",
    uniqueConstraints = @UniqueConstraint(columnNames = {"ticket_id", "blocked_by_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class TicketDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The ticket that is blocked */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /** The ticket that blocks it */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_by_id", nullable = false)
    private Ticket blockedBy;
}
