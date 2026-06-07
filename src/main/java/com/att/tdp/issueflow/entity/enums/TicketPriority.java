package com.att.tdp.issueflow.entity.enums;

/**
 * Ordered so that ordinal() reflects the escalation progression:
 * LOW(0) → MEDIUM(1) → HIGH(2) → CRITICAL(3)
 */
/**
 * Role: Enum defining the urgency levels for a ticket.
 * It is ordered from LOW to CRITICAL so that ordinal values can be used to naturally reflect the escalation progression.
 */
public enum TicketPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
