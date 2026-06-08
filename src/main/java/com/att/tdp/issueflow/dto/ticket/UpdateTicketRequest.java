package com.att.tdp.issueflow.dto.ticket;

import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
/**
 * Role: Data Transfer Object representing the payload to update an existing ticket.
 * It contains optional fields that a client can modify, such as status, priority, assignee, and due date.
 */
public class UpdateTicketRequest {
    @Size(min = 1, max = 300, message = "Title must not exceed 300 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private TicketStatus status;
    private TicketPriority priority;
    private Long assigneeId;

    @FutureOrPresent(message = "Due date must not be in the past")
    private OffsetDateTime dueDate;
}
