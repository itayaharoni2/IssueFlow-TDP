package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Role: Data Access Object for Comment entities.
 * It provides standard CRUD operations and allows retrieving a chronologically ordered list of comments for a given ticket.
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Retrieves all comments for a specific ticket, ordered by creation time from oldest to newest.
     */
    List<Comment> findAllByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
