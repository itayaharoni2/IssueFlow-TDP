package com.att.tdp.issueflow.dto.user;

import com.att.tdp.issueflow.dto.comment.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Role: Data Transfer Object for mentions response.
 */
public class MentionsResponse {
    private List<CommentResponse> data;
    private long total;
    private int page;
}
