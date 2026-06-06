package com.att.tdp.issueflow.entity.enums;

/**
 * Ordered so that ordinal() reflects the forward-only lifecycle progression:
 * TODO(0) → IN_PROGRESS(1) → IN_REVIEW(2) → DONE(3)
 */
public enum TicketStatus {
    TODO,
    IN_PROGRESS,
    IN_REVIEW,
    DONE
}
