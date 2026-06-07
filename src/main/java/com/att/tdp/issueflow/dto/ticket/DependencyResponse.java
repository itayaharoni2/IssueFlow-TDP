package com.att.tdp.issueflow.dto.ticket;

import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing a ticket dependency relationship.
 * It provides a lightweight view of a related ticket, exposing just its ID, title, and current status.
 */
public class DependencyResponse {
    private Long id;
    private String title;
    private TicketStatus status;

    /**
     * Constructs a DependencyResponse object from a given Ticket entity.
     */
    public DependencyResponse(Ticket ticket) {
        this.id = ticket.getId();
        this.title = ticket.getTitle();
        this.status = ticket.getStatus();
    }
}
