package com.att.tdp.issueflow.dto.ticket;

import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
/**
 * Role: Data Transfer Object representing the payload to update an existing ticket.
 * It contains optional fields that a client can modify, such as status, priority, assignee, and due date.
 */
public class UpdateTicketRequest {
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private Long assigneeId;
    private OffsetDateTime dueDate;
}
