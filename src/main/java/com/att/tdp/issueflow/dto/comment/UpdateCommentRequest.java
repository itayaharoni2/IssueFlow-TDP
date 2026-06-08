package com.att.tdp.issueflow.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object representing the payload to update an existing comment.
 * It ensures the client provides valid, non-blank content when making modifications.
 */
public class UpdateCommentRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 3000, message = "Content must not exceed 3000 characters")
    private String content;
}
