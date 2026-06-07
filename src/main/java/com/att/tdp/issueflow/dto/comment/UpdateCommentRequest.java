package com.att.tdp.issueflow.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing the payload to update an existing comment.
 * It ensures the client provides valid, non-blank content when making modifications.
 */
public class UpdateCommentRequest {

    @NotBlank(message = "Content is required")
    private String content;
}
