package com.att.tdp.issueflow.dto.ticket;

import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import com.att.tdp.issueflow.entity.enums.TicketType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CreateTicketRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private TicketStatus status = TicketStatus.TODO;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    @NotNull(message = "Type is required")
    private TicketType type;

    @NotNull(message = "ProjectId is required")
    private Long projectId;

    private Long assigneeId;

    private OffsetDateTime dueDate;
}
