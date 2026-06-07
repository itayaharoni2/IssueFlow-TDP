package com.att.tdp.issueflow.entity.enums;

/**
 * Role: Enum defining the various actions that can be logged in the audit system.
 * It covers standard CRUD operations as well as specific workflow actions like auto-assignment and escalation.
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    DELETE,
    RESTORE,
    AUTO_ASSIGN,
    ESCALATE
}
