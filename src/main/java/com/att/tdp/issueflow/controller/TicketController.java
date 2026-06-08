package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.ticket.CreateTicketRequest;
import com.att.tdp.issueflow.dto.ticket.ImportResultResponse;
import com.att.tdp.issueflow.dto.ticket.TicketResponse;
import com.att.tdp.issueflow.dto.ticket.UpdateTicketRequest;
import com.att.tdp.issueflow.dto.common.PaginatedResponse;
import com.att.tdp.issueflow.service.TicketService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
/**
 * Role: Provides REST API endpoints for managing tickets.
 * It handles the full lifecycle of tickets including creation, updates, soft
 * deletion, and CSV import/export.
 */
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    // Retrieves a list of all active tickets associated with a specific project.
    public ResponseEntity<List<TicketResponse>> getTicketsByProject(
            @RequestParam Long projectId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ticketService.getActiveTickets(projectId, PageRequest.of(page - 1, size)));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    // Retrieves a list of all soft-deleted tickets for a specific project. Requires ADMIN
    // privileges.
    public ResponseEntity<List<TicketResponse>> getDeletedTickets(
            @RequestParam Long projectId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(ticketService.getDeletedTickets(projectId, PageRequest.of(page - 1, size)));
    }

    // Export tickets to CSV
    @GetMapping("/export")
    // Exports all tickets of a specific project to a CSV file and streams it in the
    // HTTP response.
    public void exportTickets(@RequestParam Long projectId, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tickets.csv\"");
        ticketService.exportTicketsToCsv(projectId, response.getWriter());
    }

    // Imports multiple tickets into a specific project from an uploaded CSV file.
    @PostMapping("/import")
    public ResponseEntity<ImportResultResponse> importTickets(
            @RequestParam("projectId") Long projectId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ticketService.importTicketsFromCsv(projectId, file));
    }

    @GetMapping("/{ticketId}")
    // Retrieves the details of a specific ticket by its ID.
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getTicketById(ticketId));
    }

    @PostMapping
    // Creates a new ticket within a project using the provided details.
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.ok(ticketService.createTicket(request));
    }

    @PatchMapping("/{ticketId}")
    // Updates the properties of an existing ticket.
    public ResponseEntity<Void> updateTicket(@PathVariable Long ticketId, @Valid @RequestBody UpdateTicketRequest request) {
        ticketService.updateTicket(ticketId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{ticketId}")
    // Soft deletes a specific ticket, effectively archiving it without permanent
    // removal.
    public ResponseEntity<Void> softDeleteTicket(@PathVariable Long ticketId) {
        ticketService.softDeleteTicket(ticketId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{ticketId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    // Restores a previously soft-deleted ticket back to an active state. Requires
    // ADMIN privileges.
    public ResponseEntity<Void> restoreTicket(@PathVariable Long ticketId) {
        ticketService.restoreTicket(ticketId);
        return ResponseEntity.ok().build();
    }
}
