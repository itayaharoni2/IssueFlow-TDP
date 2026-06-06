package com.att.tdp.issueflow.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotNull(message = "AuthorId is required")
    private Long authorId;

    @NotBlank(message = "Content is required")
    private String content;
}
