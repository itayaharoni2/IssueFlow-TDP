package com.att.tdp.issueflow.dto.ticket;

import com.att.tdp.issueflow.entity.Attachment;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing an attachment sent to the client.
 * It contains the metadata of the uploaded file such as its ID, filename, content type, and associated ticket.
 */
public class AttachmentResponse {
    private Long id;
    private Long ticketId;
    private String filename;
    private String contentType;

    /**
     * Constructs an AttachmentResponse object from a given Attachment entity.
     */
    public AttachmentResponse(Attachment attachment) {
        this.id = attachment.getId();
        this.ticketId = attachment.getTicket().getId();
        this.filename = attachment.getFilename();
        this.contentType = attachment.getContentType();
    }
}
