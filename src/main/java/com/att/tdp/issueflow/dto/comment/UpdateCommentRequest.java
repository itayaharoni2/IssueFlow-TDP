package com.att.tdp.issueflow.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
/**
 * Role: Data Transfer Object for update comment request.
 */
public class UpdateCommentRequest {

    @NotBlank(message = "Content is required")
    private String content;
}
