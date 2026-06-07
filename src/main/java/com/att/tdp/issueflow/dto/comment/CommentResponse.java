package com.att.tdp.issueflow.dto.comment;

import com.att.tdp.issueflow.entity.Comment;
import lombok.Data;

import java.util.List;

@Data
/**
 * Role: Data Transfer Object representing a comment sent back to the client.
 * It contains the comment's content, the author and ticket it belongs to, and a list of any users mentioned in it.
 */
public class CommentResponse {
    private Long id;
    private Long ticketId;
    private Long authorId;
    private String content;
    private List<MentionedUserDto> mentionedUsers;

    /**
     * Constructs a CommentResponse based on a Comment entity and a list of its mentioned users.
     */
    public CommentResponse(Comment comment, List<MentionedUserDto> mentionedUsers) {
        this.id = comment.getId();
        this.ticketId = comment.getTicket().getId();
        this.authorId = comment.getAuthor().getId();
        this.content = comment.getContent();
        this.mentionedUsers = mentionedUsers;
    }
}
