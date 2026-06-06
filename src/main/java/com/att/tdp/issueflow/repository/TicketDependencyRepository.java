package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.TicketDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketDependencyRepository extends JpaRepository<TicketDependency, Long> {

    List<TicketDependency> findAllByTicketId(Long ticketId);

    boolean existsByTicketIdAndBlockedById(Long ticketId, Long blockedById);
}
