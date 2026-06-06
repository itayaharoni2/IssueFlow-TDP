package com.att.tdp.issueflow.entity;

import com.att.tdp.issueflow.entity.enums.AuditAction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditAction action;

    /** e.g. "Ticket", "Project", "Comment", "User" */
    @Column(nullable = false, length = 50)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    /**
     * "USER" or "SYSTEM" — indicates who performed the action.
     * SYSTEM is used for AUTO_ASSIGN and ESCALATE actions.
     */
    @Column(nullable = false, length = 10)
    private String actor;

    /** Username of the user who performed the action; null when actor=SYSTEM */
    @Column(length = 100)
    private String performedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
