package com.att.tdp.issueflow.dto.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object for create dependency request.
 */
public class CreateDependencyRequest {
    @NotNull(message = "BlockedBy ID is required")
    private Long blockedBy;
}
