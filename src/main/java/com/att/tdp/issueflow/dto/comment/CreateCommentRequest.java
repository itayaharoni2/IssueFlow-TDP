package com.att.tdp.issueflow.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing the payload to create a new comment.
 * It validates that an author ID and non-empty content are provided when a client submits a new comment.
 */
public class CreateCommentRequest {

    @NotNull(message = "AuthorId is required")
    private Long authorId;

    @NotBlank(message = "Content is required")
    private String content;
}
