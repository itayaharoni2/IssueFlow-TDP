package com.att.tdp.issueflow.dto.ticket;

import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import com.att.tdp.issueflow.entity.enums.TicketType;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
/**
 * Role: Data Transfer Object representing a ticket returned to the client.
 * It encapsulates comprehensive ticket details including status, priority, type, project ID, assignee, and due date information.
 */
public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private TicketType type;
    private Long projectId;
    private Long assigneeId;
    private OffsetDateTime dueDate;
    private boolean isOverdue;

    /**
     * Constructs a TicketResponse object from a given Ticket entity, computing dynamic fields like isOverdue.
     */
    public TicketResponse(Ticket ticket) {
        this.id = ticket.getId();
        this.title = ticket.getTitle();
        this.description = ticket.getDescription();
        this.status = ticket.getStatus();
        this.priority = ticket.getPriority();
        this.type = ticket.getType();
        this.projectId = ticket.getProject().getId();
        this.assigneeId = ticket.getAssignee() != null ? ticket.getAssignee().getId() : null;
        this.dueDate = ticket.getDueDate();
        this.isOverdue = ticket.isOverdue();
    }
}
