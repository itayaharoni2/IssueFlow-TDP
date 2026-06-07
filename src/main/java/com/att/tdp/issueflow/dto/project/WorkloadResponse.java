package com.att.tdp.issueflow.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * Role: Data Transfer Object representing the workload statistics for a specific user.
 * It contains the user's basic info and the count of open tickets currently assigned to them.
 */
public class WorkloadResponse {
    private Long userId;
    private String username;
    private long openTicketCount;
}
