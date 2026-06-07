package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.comment.CommentResponse;
import com.att.tdp.issueflow.dto.comment.CreateCommentRequest;
import com.att.tdp.issueflow.dto.comment.UpdateCommentRequest;
import com.att.tdp.issueflow.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets/{ticketId}/comments")
@RequiredArgsConstructor
/**
 * Role: Provides REST API endpoints for managing ticket comments.
 * It supports retrieving, creating, updating, and deleting comments on specific tickets via the CommentService.
 */
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    /**
     * Retrieves all comments associated with a specific ticket.
     */
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(commentService.getCommentsByTicket(ticketId));
    }

    @PostMapping
    /**
     * Adds a new comment to a specific ticket.
     */
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long ticketId, @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(commentService.createComment(ticketId, request));
    }

    @PatchMapping("/{commentId}")
    /**
     * Updates the content of an existing comment on a ticket.
     */
    public ResponseEntity<Void> updateComment(@PathVariable Long ticketId, @PathVariable Long commentId, @Valid @RequestBody UpdateCommentRequest request) {
        commentService.updateComment(ticketId, commentId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}")
    /**
     * Deletes a specific comment from a ticket.
     */
    public ResponseEntity<Void> deleteComment(@PathVariable Long ticketId, @PathVariable Long commentId) {
        commentService.deleteComment(ticketId, commentId);
        return ResponseEntity.ok().build();
    }
}
