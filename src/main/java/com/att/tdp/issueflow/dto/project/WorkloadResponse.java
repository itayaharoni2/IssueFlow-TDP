package com.att.tdp.issueflow.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/**
 * Role: Data Transfer Object for workload response.
 */
public class WorkloadResponse {
    private Long userId;
    private String username;
    private long openTicketCount;
}
