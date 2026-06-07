package com.att.tdp.issueflow.dto.audit;

import com.att.tdp.issueflow.entity.AuditLog;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * Role: Data Transfer Object representing a single audit log entry returned to the client.
 * It encapsulates the details of a system event, including the action, entity, actor, and timestamp.
 */
public class AuditLogResponse {
    private Long id;
    private AuditAction action;
    private String entityType;
    private Long entityId;
    private Long performedBy;
    private String actor;
    private LocalDateTime timestamp;

    /**
     * Constructs an AuditLogResponse object from a given AuditLog entity, mapping its properties.
     */
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
