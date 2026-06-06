package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/**
 * Role: Handles database access and queries for ticket.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByProjectIdAndDeletedAtIsNull(Long projectId);

    List<Ticket> findAllByProjectIdAndDeletedAtIsNotNull(Long projectId);

    Optional<Ticket> findByIdAndDeletedAtIsNull(Long id);

    long countByProjectIdAndAssigneeIdAndStatusNotAndDeletedAtIsNull(Long projectId, Long assigneeId, com.att.tdp.issueflow.entity.enums.TicketStatus status);

    List<Ticket> findByDueDateBeforeAndStatusNotAndDeletedAtIsNull(java.time.OffsetDateTime dueDate, com.att.tdp.issueflow.entity.enums.TicketStatus status);
}
