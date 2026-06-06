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
 * Role: Provides REST API endpoints for comment.
 */
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    /**
     * Retrieves comments.
     */
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(commentService.getCommentsByTicket(ticketId));
    }

    @PostMapping
    /**
     * Executes the add comment operation.
     */
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long ticketId, @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(commentService.createComment(ticketId, request));
    }

    @PatchMapping("/{commentId}")
    /**
     * Updates an existing comment.
     */
    public ResponseEntity<Void> updateComment(@PathVariable Long ticketId, @PathVariable Long commentId, @Valid @RequestBody UpdateCommentRequest request) {
        commentService.updateComment(ticketId, commentId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}")
    /**
     * Deletes comment.
     */
    public ResponseEntity<Void> deleteComment(@PathVariable Long ticketId, @PathVariable Long commentId) {
        commentService.deleteComment(ticketId, commentId);
        return ResponseEntity.ok().build();
    }
}
