package com.att.tdp.issueflow.dto.comment;

import com.att.tdp.issueflow.entity.Comment;
import lombok.Data;

import java.util.List;

@Data
/**
 * Role: Data Transfer Object for comment response.
 */
public class CommentResponse {
    private Long id;
    private Long ticketId;
    private Long authorId;
    private String content;
    private List<MentionedUserDto> mentionedUsers;

    public CommentResponse(Comment comment, List<MentionedUserDto> mentionedUsers) {
        this.id = comment.getId();
        this.ticketId = comment.getTicket().getId();
        this.authorId = comment.getAuthor().getId();
        this.content = comment.getContent();
        this.mentionedUsers = mentionedUsers;
    }
}
