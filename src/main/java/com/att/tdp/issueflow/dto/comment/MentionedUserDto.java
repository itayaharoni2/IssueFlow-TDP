package com.att.tdp.issueflow.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Role: Data Transfer Object for mentioned user dto.
 */
public class MentionedUserDto {
    private Long id;
    private String username;
    private String fullName;
}
