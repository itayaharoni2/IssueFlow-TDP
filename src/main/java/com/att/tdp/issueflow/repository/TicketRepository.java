package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
/**
 * Role: Data Access Object for Ticket entities.
 * It provides custom queries to filter active/deleted tickets, track assignee workloads, and identify overdue tasks.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Retrieves all active (non-deleted) tickets associated with a specific project.
     */
    List<Ticket> findAllByProjectIdAndDeletedAtIsNull(Long projectId);

    /**
     * Retrieves all soft-deleted tickets associated with a specific project.
     */
    List<Ticket> findAllByProjectIdAndDeletedAtIsNotNull(Long projectId);

    /**
     * Retrieves a specific ticket by its ID, ensuring it has not been soft-deleted.
     */
    Optional<Ticket> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Counts the number of active, non-completed tickets assigned to a specific user in a given project.
     */
    long countByProjectIdAndAssigneeIdAndStatusNotAndDeletedAtIsNull(Long projectId, Long assigneeId, com.att.tdp.issueflow.entity.enums.TicketStatus status);

    /**
     * Retrieves all active tickets that are past their due date and have not yet been completed.
     */
    List<Ticket> findByDueDateBeforeAndStatusNotAndDeletedAtIsNull(java.time.OffsetDateTime dueDate, com.att.tdp.issueflow.entity.enums.TicketStatus status);
}
