package com.att.tdp.issueflow.dto.ticket;

import com.att.tdp.issueflow.entity.Attachment;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object for attachment response.
 */
public class AttachmentResponse {
    private Long id;
    private Long ticketId;
    private String filename;
    private String contentType;

    public AttachmentResponse(Attachment attachment) {
        this.id = attachment.getId();
        this.ticketId = attachment.getTicket().getId();
        this.filename = attachment.getFilename();
        this.contentType = attachment.getContentType();
    }
}
