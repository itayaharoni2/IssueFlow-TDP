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
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String entityType, Long entityId, Long performedByUserId, String actor) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setPerformedByUserId(performedByUserId);
        auditLog.setActor(actor);
        auditLogRepository.save(auditLog);
    }

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
