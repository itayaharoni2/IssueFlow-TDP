package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.ticket.CreateTicketRequest;
import com.att.tdp.issueflow.dto.ticket.TicketResponse;
import com.att.tdp.issueflow.dto.ticket.UpdateTicketRequest;
import com.att.tdp.issueflow.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getTicketsByProject(@RequestParam Long projectId) {
        return ResponseEntity.ok(ticketService.getActiveTickets(projectId));
    }

    // Must be mapped before /{ticketId} to avoid collision
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketResponse>> getDeletedTickets(@RequestParam Long projectId) {
        return ResponseEntity.ok(ticketService.getDeletedTickets(projectId));
    }
    
    // Stub for Phase 5 export (must be mapped before /{ticketId})
    @GetMapping("/export")
    public ResponseEntity<Void> exportTickets(@RequestParam Long projectId) {
        return ResponseEntity.ok().build();
    }
    
    // Stub for Phase 5 import (must be mapped before /{ticketId})
    @PostMapping("/import")
    public ResponseEntity<Void> importTickets() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getTicketById(ticketId));
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.ok(ticketService.createTicket(request));
    }

    @PatchMapping("/{ticketId}")
    public ResponseEntity<Void> updateTicket(@PathVariable Long ticketId, @RequestBody UpdateTicketRequest request) {
        ticketService.updateTicket(ticketId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> softDeleteTicket(@PathVariable Long ticketId) {
        ticketService.softDeleteTicket(ticketId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{ticketId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restoreTicket(@PathVariable Long ticketId) {
        ticketService.restoreTicket(ticketId);
        return ResponseEntity.ok().build();
    }
}
