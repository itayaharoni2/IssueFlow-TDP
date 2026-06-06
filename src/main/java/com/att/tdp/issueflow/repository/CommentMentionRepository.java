package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.entity.CommentMention;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Role: Handles database access and queries for comment mention.
 */
public interface CommentMentionRepository extends JpaRepository<CommentMention, Long> {
    void deleteByCommentId(Long commentId);
    Page<CommentMention> findByUserId(Long userId, Pageable pageable);
    List<CommentMention> findByCommentId(Long commentId);
}
