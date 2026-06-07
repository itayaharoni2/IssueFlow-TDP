package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.CommentMention;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Role: Data Access Object for CommentMention entities.
 * It manages the many-to-many relationship between comments and mentioned users, supporting pagination and bulk deletion.
 */
public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {
    /**
     * Deletes all mentions associated with a given comment, useful during comment updates or deletions.
     */
    void deleteByCommentId(Long commentId);

    /**
     * Retrieves a paginated list of mentions for a specific user to power their notifications feed.
     */
    Page<CommentMention> findByUserId(Long userId, Pageable pageable);

    /**
     * Retrieves all user mentions associated with a specific comment.
     */
    List<CommentMention> findByCommentId(Long commentId);
}
