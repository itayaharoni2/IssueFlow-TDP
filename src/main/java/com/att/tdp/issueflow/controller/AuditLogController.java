package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.audit.AuditLogResponse;
import com.att.tdp.issueflow.dto.common.PaginatedResponse;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import com.att.tdp.issueflow.service.AuditLogService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@Validated
/**
 * Role: Provides REST API endpoints for querying system audit logs.
 * It exposes read-only operations to fetch audit trails based on entity types,
 * IDs, actions, or specific actors.
 */
public class AuditLogController {

    private final AuditLogService auditLogService;

    // Retrieves a list of audit logs, optionally filtered by entity type, entity
    // ID, action, or actor.
    @GetMapping
    public ResponseEntity<PaginatedResponse<AuditLogResponse>> getLogs(
            @RequestParam(required = false) @Size(max = 50, message = "Entity type must not exceed 50 characters") String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) @Size(max = 10, message = "Actor must not exceed 10 characters") String actor,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(auditLogService.getLogs(entityType, entityId, action, actor, PageRequest.of(page - 1, size)));
    }
}
