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
 * Role: Data Transfer Object representing a paginated response of user mentions.
 * It contains a list of comments where the user was mentioned, along with total count and current page information.
 */
public class MentionsResponse {
    private List<CommentResponse> data;
    private long total;
    private int page;
}
