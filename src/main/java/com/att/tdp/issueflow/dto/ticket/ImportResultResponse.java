package com.att.tdp.issueflow.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Role: Data Transfer Object representing the outcome of a bulk ticket import operation.
 * It provides the number of successfully created tickets, the number of failures, and a list of specific error messages.
 */
public class ImportResultResponse {
    private int created;
    private int failed;
    private List<String> errors;
}
