package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.ticket.CreateDependencyRequest;
import com.att.tdp.issueflow.dto.ticket.DependencyResponse;
import com.att.tdp.issueflow.service.DependencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets/{ticketId}/dependencies")
@RequiredArgsConstructor
/**
 * Role: Provides REST API endpoints for managing ticket dependencies.
 * It allows clients to declare and query blocker relationships between tickets, ensuring proper workflow sequencing.
 */
public class DependencyController {

    private final DependencyService dependencyService;

    @PostMapping
    /**
     * Declares that a specific ticket is blocked by another ticket.
     */
    public ResponseEntity<Void> addDependency(@PathVariable Long ticketId,
                                              @Valid @RequestBody CreateDependencyRequest request) {
        dependencyService.addDependency(ticketId, request.getBlockedBy());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    /**
     * Retrieves all dependencies (blockers and blocked tickets) for a specific ticket.
     */
    public ResponseEntity<List<DependencyResponse>> getDependencies(@PathVariable Long ticketId) {
        return ResponseEntity.ok(dependencyService.getDependencies(ticketId));
    }

    @DeleteMapping("/{blockerId}")
    /**
     * Removes an existing dependency relationship between two tickets.
     */
    public ResponseEntity<Void> removeDependency(@PathVariable Long ticketId,
                                                 @PathVariable Long blockerId) {
        dependencyService.removeDependency(ticketId, blockerId);
        return ResponseEntity.ok().build();
    }
}
