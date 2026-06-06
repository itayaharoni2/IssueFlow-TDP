package com.att.tdp.issueflow.entity;

import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import com.att.tdp.issueflow.entity.enums.TicketType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.TODO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority = TicketPriority.LOW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /** Optional due date for auto-escalation scheduling (ISO-8601 with timezone, e.g. 2026-04-01T00:00:00Z) */
    @Column
    private OffsetDateTime dueDate;

    /** True when ticket is at CRITICAL priority and still overdue */
    @Column(nullable = false)
    private boolean isOverdue = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Soft-delete: null means active, non-null means deleted */
    @Column
    private LocalDateTime deletedAt;

    @Version
    private Long version;
}
