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
public class DependencyController {

    private final DependencyService dependencyService;

    @PostMapping
    public ResponseEntity<Void> addDependency(@PathVariable Long ticketId,
                                              @Valid @RequestBody CreateDependencyRequest request) {
        dependencyService.addDependency(ticketId, request.getBlockedBy());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<DependencyResponse>> getDependencies(@PathVariable Long ticketId) {
        return ResponseEntity.ok(dependencyService.getDependencies(ticketId));
    }

    @DeleteMapping("/{blockerId}")
    public ResponseEntity<Void> removeDependency(@PathVariable Long ticketId,
                                                 @PathVariable Long blockerId) {
        dependencyService.removeDependency(ticketId, blockerId);
        return ResponseEntity.ok().build();
    }
}
