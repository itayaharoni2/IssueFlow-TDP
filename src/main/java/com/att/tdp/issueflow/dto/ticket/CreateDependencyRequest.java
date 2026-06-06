package com.att.tdp.issueflow.dto.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDependencyRequest {
    @NotNull(message = "BlockedBy ID is required")
    private Long blockedBy;
}
