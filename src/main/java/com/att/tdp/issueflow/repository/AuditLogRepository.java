package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Role: Data Access Object for AuditLog entities.
 * It provides standard CRUD operations and supports complex dynamic queries via JpaSpecificationExecutor.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
}
