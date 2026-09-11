package com.fraudService.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpToolExecutionResponse {

    private String toolName;
    private String status; // SUCCESS, REQUIRES_HUMAN_CONFIRMATION, FAILED
    private Object result;
    private String confirmationPrompt;
    private String correlationId;
    private LocalDateTime timestamp;

}
