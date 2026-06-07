package com.att.tdp.issueflow.scheduler;

import com.att.tdp.issueflow.service.EscalationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
/**
 * Role: Background scheduled component responsible for automatically managing overdue tickets.
 * It periodically triggers the escalation service to raise priorities and flags on tickets that have breached their due dates.
 */
public class EscalationScheduler {

    private final EscalationService escalationService;

    // Run every minute for testing/demonstration purposes
    // Depending on rules.md, adjust cron as necessary (e.g., hourly, daily)
    @Scheduled(fixedRateString = "${app.escalation.interval:60000}")
    /**
     * Scheduled task that executes at a configurable interval to find and escalate overdue tickets.
     */
    public void runEscalation() {
        escalationService.escalateOverdueTickets();
    }
}
