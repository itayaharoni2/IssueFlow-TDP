package com.att.tdp.issueflow.entity.enums;

/**
 * Role: Enum categorizing the fundamental nature of a ticket.
 * It helps differentiate between fixing defects (BUG), adding new capabilities (FEATURE), or addressing internal debt (TECHNICAL).
 */
public enum TicketType {
    BUG,
    FEATURE,
    TECHNICAL
}
