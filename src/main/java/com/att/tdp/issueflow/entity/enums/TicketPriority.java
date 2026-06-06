package com.att.tdp.issueflow.entity.enums;

/**
 * Ordered so that ordinal() reflects the escalation progression:
 * LOW(0) → MEDIUM(1) → HIGH(2) → CRITICAL(3)
 */
/**
 * Role: Represents the ticket priority entity or object.
 */
public enum TicketPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
