package com.att.tdp.issueflow.dto.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {
    private int created;
    private int failed;
    private List<String> errors;
}
