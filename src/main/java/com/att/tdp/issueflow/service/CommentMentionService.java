package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.comment.CommentResponse;
import com.att.tdp.issueflow.dto.comment.MentionedUserDto;
import com.att.tdp.issueflow.dto.user.MentionsResponse;
import com.att.tdp.issueflow.entity.CommentMention;
import com.att.tdp.issueflow.exception.ResourceNotFoundException;
import com.att.tdp.issueflow.repository.CommentMentionRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Role: Service layer for querying user mentions across comments.
 * It powers features like notification feeds by retrieving paginated lists of comments where a specific user was mentioned.
 */
public class CommentMentionService {

    private final CommentMentionRepository commentMentionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    /**
     * Fetches a paginated list of comments in which the specified user has been mentioned, fully populated with other mentioned users.
     */
    public MentionsResponse getMentions(Long userId, int page, int pageSize) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        Page<CommentMention> mentionsPage = commentMentionRepository.findByUserId(userId, pageable);

        List<CommentResponse> data = mentionsPage.getContent().stream().map(mention -> {
            List<MentionedUserDto> allMentionsInComment = commentMentionRepository.findByCommentId(mention.getComment().getId())
                    .stream()
                    .map(cm -> new MentionedUserDto(cm.getUser().getId(), cm.getUser().getUsername(), cm.getUser().getFullName()))
                    .collect(Collectors.toList());
            return new CommentResponse(mention.getComment(), allMentionsInComment);
        }).collect(Collectors.toList());

        return new MentionsResponse(data, mentionsPage.getTotalElements(), page);
    }
}
