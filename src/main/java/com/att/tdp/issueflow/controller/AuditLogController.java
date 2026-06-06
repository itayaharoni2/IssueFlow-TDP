package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.audit.AuditLogResponse;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import com.att.tdp.issueflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String actor) {
        
        return ResponseEntity.ok(auditLogService.getLogs(entityType, entityId, action, actor));
    }
}
