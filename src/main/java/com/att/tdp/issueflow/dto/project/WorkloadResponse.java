package com.att.tdp.issueflow.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkloadResponse {
    private Long userId;
    private String username;
    private long openTicketCount;
}
