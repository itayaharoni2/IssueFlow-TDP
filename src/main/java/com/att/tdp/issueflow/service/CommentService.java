package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.comment.CommentResponse;
import com.att.tdp.issueflow.dto.comment.CreateCommentRequest;
import com.att.tdp.issueflow.dto.comment.UpdateCommentRequest;
import com.att.tdp.issueflow.entity.Comment;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.AuditAction;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.CommentMentionRepository;
import com.att.tdp.issueflow.repository.CommentRepository;
import com.att.tdp.issueflow.repository.TicketRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import com.att.tdp.issueflow.entity.CommentMention;
import com.att.tdp.issueflow.dto.comment.MentionedUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.att.tdp.issueflow.dto.common.PaginatedResponse;

@Service
@RequiredArgsConstructor
/**
 * Role: Service layer responsible for managing comments on tickets.
 * It provides operations to retrieve, create, update, and delete comments,
 * automatically parsing and handling user "@" mentions.
 */
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMentionRepository commentMentionRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    @Transactional(readOnly = true)
    // Fetches all comments associated with a specific ticket in chronological
    // order, including populated mention metadata.
    public List<CommentResponse> getCommentsByTicket(Long ticketId, Pageable pageable) {
        // Validation: Check if ticket exists
        ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        Page<Comment> page = commentRepository.findAllByTicketIdOrderByCreatedAtAsc(ticketId, pageable);
        return page.map(comment -> {
            List<MentionedUserDto> mentions = commentMentionRepository.findByCommentId(comment.getId()).stream()
                    .map(cm -> new MentionedUserDto(cm.getUser().getId(), cm.getUser().getUsername(),
                            cm.getUser().getFullName()))
                    .collect(Collectors.toList());
            return new CommentResponse(comment, mentions);
        }).getContent();
    }

    @Transactional
    // Creates a new comment on a ticket, parses it for user mentions, and logs an
    // audit trail.
    public CommentResponse createComment(Long ticketId, CreateCommentRequest request) {
        Ticket ticket = ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setContent(HtmlUtils.htmlEscape(request.getContent()));

        Comment saved = commentRepository.save(comment);

        List<MentionedUserDto> mentionedUsers = processMentions(saved, request.getContent());

        Long currentUserId = getCurrentUserId();
        auditLogService.log(AuditAction.CREATE, "Comment", saved.getId(), currentUserId, "USER");

        return new CommentResponse(saved, mentionedUsers);
    }

    @Transactional
    // Modifies the content of an existing comment, refreshes the parsed mentions,
    // and records the update in the audit log.
    public void updateComment(Long ticketId, Long commentId, UpdateCommentRequest request) {
        ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getTicket().getId().equals(ticketId)) {
            throw new IllegalArgumentException("Comment does not belong to this ticket");
        }

        if (request.getContent() != null) {
            comment.setContent(HtmlUtils.htmlEscape(request.getContent()));
            comment.setUpdatedAt(LocalDateTime.now());
            commentRepository.save(comment);
        }

        processMentions(comment, request.getContent());

        auditLogService.log(AuditAction.UPDATE, "Comment", comment.getId(), getCurrentUserId(), "USER");
    }

    @Transactional
    // Removes a comment and its associated mentions from the database, leaving an
    // audit log of the deletion.
    public void deleteComment(Long ticketId, Long commentId) {
        ticketRepository.findByIdAndDeletedAtIsNull(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getTicket().getId().equals(ticketId)) {
            throw new IllegalArgumentException("Comment does not belong to this ticket");
        }

        commentMentionRepository.deleteByCommentId(commentId);
        commentRepository.delete(comment);

        auditLogService.log(AuditAction.DELETE, "Comment", commentId, getCurrentUserId(), "USER");
    }

    // Helper method to extract the ID of the currently authenticated user from the
    // security context.
    private Long getCurrentUserId() {
        try {
            return authService.getCurrentUser().getId();
        } catch (Exception e) {
            return null; // For test cases without auth
        }
    }

    // Parses the comment body for "@username" patterns, looks up the corresponding
    // users, and links them as mentions.
    private List<MentionedUserDto> processMentions(Comment comment, String content) {
        commentMentionRepository.deleteByCommentId(comment.getId());

        List<MentionedUserDto> mentionedUsers = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return mentionedUsers;
        }

        Set<String> usernames = new HashSet<>();
        Pattern pattern = Pattern.compile("(?<=^|\\s)@([a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            usernames.add(matcher.group(1).toLowerCase());
        }

        for (String username : usernames) {
            userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
                CommentMention mention = new CommentMention();
                mention.setComment(comment);
                mention.setUser(user);
                commentMentionRepository.save(mention);
                mentionedUsers.add(new MentionedUserDto(user.getId(), user.getUsername(), user.getFullName()));
            });
        }
        return mentionedUsers;
    }
}
