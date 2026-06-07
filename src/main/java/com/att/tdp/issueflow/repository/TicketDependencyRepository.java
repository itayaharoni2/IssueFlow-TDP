package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.TicketDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Role: Data Access Object for TicketDependency entities.
 * It manages the "blocked by" relationships between tickets, ensuring workflows can be enforced.
 */
public interface TicketDependencyRepository extends JpaRepository<TicketDependency, Long> {

    /**
     * Retrieves all dependencies (blockers) for a specific ticket.
     */
    List<TicketDependency> findAllByTicketId(Long ticketId);

    /**
     * Checks if a specific dependency relationship already exists between two tickets.
     */
    boolean existsByTicketIdAndBlockedById(Long ticketId, Long blockedById);
}
