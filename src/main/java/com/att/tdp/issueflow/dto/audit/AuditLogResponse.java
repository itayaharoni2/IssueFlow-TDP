package com.att.tdp.issueflow.dto.audit;

import com.att.tdp.issueflow.entity.AuditLog;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * Role: Data Transfer Object for audit log response.
 */
public class AuditLogResponse {
    private Long id;
    private AuditAction action;
    private String entityType;
    private Long entityId;
    private Long performedBy;
    private String actor;
    private LocalDateTime timestamp;

    public AuditLogResponse(AuditLog auditLog) {
        this.id = auditLog.getId();
        this.action = auditLog.getAction();
        this.entityType = auditLog.getEntityType();
        this.entityId = auditLog.getEntityId();
        this.performedBy = auditLog.getPerformedByUserId();
        this.actor = auditLog.getActor();
        this.timestamp = auditLog.getTimestamp();
    }
}
