package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.ticket.AttachmentResponse;
import com.att.tdp.issueflow.entity.Attachment;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.AttachmentRepository;
import com.att.tdp.issueflow.repository.TicketRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
/**
 * Role: Service layer responsible for processing file attachments on tickets.
 * It handles the uploading, validation (e.g. content types), storage, and deletion of attachments while writing audit logs.
 */
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/png", "image/jpeg", "application/pdf", "text/plain"
    );

    private static final List<String> BLOCKED_EXTENSIONS = List.of(
            ".exe", ".sh", ".bat", ".cmd", ".ps1", ".jar", ".war", ".msi", ".com", ".vbs", ".js"
    );

    @Transactional
    /**
     * Validates and saves an uploaded file as an attachment to the specified ticket, generating an audit log.
     */
    public AttachmentResponse uploadAttachment(Long ticketId, MultipartFile file) {
        Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid content type");
        }

        User uploader = getCurrentUserEntity();

        Attachment attachment = new Attachment();
        attachment.setTicket(ticket);
        attachment.setFilename(sanitizeFilename(file.getOriginalFilename()));
        attachment.setContentType(contentType);
        attachment.setUploadedBy(uploader);
        try {
            attachment.setData(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file data", e);
        }

        Attachment saved = attachmentRepository.save(attachment);

        auditLogService.log(AuditAction.CREATE, "Attachment", saved.getId(), uploader.getId(), "USER");

        return new AttachmentResponse(saved);
    }

    @Transactional
    /**
     * Removes an attachment from a ticket and logs the deletion action.
     */
    public void deleteAttachment(Long ticketId, Long attachmentId) {
        Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));

        if (!attachment.getTicket().getId().equals(ticket.getId())) {
            throw new IllegalArgumentException("Attachment does not belong to this ticket");
        }

        attachmentRepository.delete(attachment);

        Long currentUserId = getCurrentUserId();
        auditLogService.log(AuditAction.DELETE, "Attachment", attachmentId, currentUserId, "USER");
    }

    /**
     * Helper method to retrieve the ID of the currently authenticated user from the security context.
     */
    private Long getCurrentUserId() {
        try {
            return authService.getCurrentUser().getId();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Helper method to fetch the full User entity for the currently authenticated user.
     */
    private User getCurrentUserEntity() {
        Long id = getCurrentUserId();
        if (id == null) {
            throw new RuntimeException("No authenticated user");
        }
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Sanitizes the filename by removing path traversal characters and checking for dangerous extensions.
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new com.att.tdp.issueflow.exception.BadRequestException("Filename cannot be empty");
        }

        // Prevent path traversal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new com.att.tdp.issueflow.exception.BadRequestException("Filename contains invalid characters (path traversal)");
        }

        // Check length
        if (filename.length() > 255) {
            throw new com.att.tdp.issueflow.exception.BadRequestException("Filename is too long (max 255 chars)");
        }

        // Check for blocked extensions
        String lowerCaseFilename = filename.toLowerCase();
        for (String ext : BLOCKED_EXTENSIONS) {
            if (lowerCaseFilename.endsWith(ext)) {
                throw new com.att.tdp.issueflow.exception.BadRequestException("File extension is not allowed for security reasons");
            }
        }

        return filename;
    }
}
