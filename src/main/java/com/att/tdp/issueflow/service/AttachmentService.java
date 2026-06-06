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
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/png", "image/jpeg", "application/pdf", "text/plain"
    );

    @Transactional
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
        attachment.setFilename(file.getOriginalFilename());
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

    private Long getCurrentUserId() {
        try {
            return authService.getCurrentUser().getId();
        } catch (Exception e) {
            return null;
        }
    }

    private User getCurrentUserEntity() {
        Long id = getCurrentUserId();
        if (id == null) {
            throw new RuntimeException("No authenticated user");
        }
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
