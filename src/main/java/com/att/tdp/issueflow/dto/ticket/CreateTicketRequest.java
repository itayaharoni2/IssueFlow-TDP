package com.att.tdp.issueflow.dto.ticket;

import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import com.att.tdp.issueflow.entity.enums.TicketType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
/**
 * Role: Data Transfer Object representing the payload to create a new ticket.
 * It encapsulates required fields like title, priority, type, and project ID, alongside optional fields like assignee and due date.
 */
public class CreateTicketRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 300, message = "Title must not exceed 300 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private TicketStatus status = TicketStatus.TODO;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    @NotNull(message = "Type is required")
    private TicketType type;

    @NotNull(message = "ProjectId is required")
    private Long projectId;

    private Long assigneeId;

    @FutureOrPresent(message = "Due date must not be in the past")
    private OffsetDateTime dueDate;
}

