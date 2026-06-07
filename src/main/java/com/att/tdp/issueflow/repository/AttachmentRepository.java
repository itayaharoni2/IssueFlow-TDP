package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Role: Data Access Object for Attachment entities.
 * It provides standard CRUD operations and custom query methods to retrieve attachments by their associated ticket ID.
 */
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    /**
     * Retrieves all attachments associated with a specific ticket.
     */
    List<Attachment> findByTicketId(Long ticketId);
}
