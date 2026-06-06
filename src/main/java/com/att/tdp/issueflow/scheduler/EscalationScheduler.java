package com.att.tdp.issueflow.scheduler;

import com.att.tdp.issueflow.service.EscalationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
/**
 * Role: Represents the escalation scheduler entity or object.
 */
public class EscalationScheduler {

    private final EscalationService escalationService;

    // Run every minute for testing/demonstration purposes
    // Depending on rules.md, adjust cron as necessary (e.g., hourly, daily)
    @Scheduled(fixedRateString = "${app.escalation.interval:60000}")
    /**
     * Executes the run escalation operation.
     */
    public void runEscalation() {
        escalationService.escalateOverdueTickets();
    }
}
