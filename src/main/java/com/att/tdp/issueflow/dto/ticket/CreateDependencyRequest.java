package com.att.tdp.issueflow.dto.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing the payload to create a new ticket dependency.
 * It ensures the client provides the ID of the ticket that acts as a blocker.
 */
public class CreateDependencyRequest {
    @NotNull(message = "BlockedBy ID is required")
    private Long blockedBy;
}
