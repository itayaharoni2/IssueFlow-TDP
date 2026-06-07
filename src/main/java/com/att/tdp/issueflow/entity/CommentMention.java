package com.att.tdp.issueflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Join entity tracking which users are mentioned in a comment.
 * Composite unique constraint prevents duplicate (comment_id, user_id) pairs.
 * Mentions are deleted and re-inserted on every comment update.
 */
@Entity
@Table(
    name = "comment_mentions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
/**
 * Role: Represents a many-to-many join entity tracking user mentions within comments.
 * It links a specific comment to a mentioned user, ensuring notifications and queries can efficiently find relevant mentions.
 */
public class CommentMention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
