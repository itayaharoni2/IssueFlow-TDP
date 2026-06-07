package com.att.tdp.issueflow.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Role: Data Transfer Object representing a user who was mentioned in a comment.
 * It encapsulates basic user details like ID, username, and full name for quick reference in API responses.
 */
public class MentionedUserDto {
    private Long id;
    private String username;
    private String fullName;
}
