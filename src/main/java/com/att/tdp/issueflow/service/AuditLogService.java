package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.entity.AuditLog;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import com.att.tdp.issueflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
/**
 * Role: Service layer responsible for maintaining system audit trails.
 * It provides methods to persist audit events and query logs based on various filters like entity type or actor.
 */
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    /**
     * Records a new audit log entry in an independent transaction, ensuring it commits even if the parent transaction fails.
     */
    public void log(AuditAction action, String entityType, Long entityId, Long performedByUserId, String actor) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setPerformedByUserId(performedByUserId);
        auditLog.setActor(actor);
        auditLogRepository.save(auditLog);
    }

    /**
     * Retrieves and filters audit logs based on the provided parameters, mapping them to response DTOs.
     */
    @Transactional(readOnly = true)
    public java.util.List<com.att.tdp.issueflow.dto.audit.AuditLogResponse> getLogs(String entityType, Long entityId, AuditAction action, String actor) {
        // For Phase 3, we just return all logs or do simple filtering
        // We'll use simple stream filtering for now, Specification can be used later
        return auditLogRepository.findAll().stream()
                .filter(log -> entityType == null || entityType.equals(log.getEntityType()))
                .filter(log -> entityId == null || entityId.equals(log.getEntityId()))
                .filter(log -> action == null || action.equals(log.getAction()))
                .filter(log -> actor == null || actor.equals(log.getActor()))
                .map(com.att.tdp.issueflow.dto.audit.AuditLogResponse::new)
                .collect(java.util.stream.Collectors.toList());
    }
}
