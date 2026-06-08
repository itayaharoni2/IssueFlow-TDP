package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.entity.AuditLog;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import com.att.tdp.issueflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.att.tdp.issueflow.dto.common.PaginatedResponse;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

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
    public PaginatedResponse<com.att.tdp.issueflow.dto.audit.AuditLogResponse> getLogs(String entityType, Long entityId, AuditAction action, String actor, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (entityType != null) {
                predicates.add(cb.equal(root.get("entityType"), entityType));
            }
            if (entityId != null) {
                predicates.add(cb.equal(root.get("entityId"), entityId));
            }
            if (action != null) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (actor != null) {
                predicates.add(cb.equal(root.get("actor"), actor));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);
        return new PaginatedResponse<>(page.map(com.att.tdp.issueflow.dto.audit.AuditLogResponse::new));
    }
}
