package com.att.tdp.issueflow.dto.ticket;

import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import lombok.Data;

@Data
public class DependencyResponse {
    private Long id;
    private String title;
    private TicketStatus status;

    public DependencyResponse(Ticket ticket) {
        this.id = ticket.getId();
        this.title = ticket.getTitle();
        this.status = ticket.getStatus();
    }
}
