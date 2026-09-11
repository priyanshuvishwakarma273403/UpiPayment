package com.fraudService.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpToolDefinition {

    private String name;
    private String description;
    private String category; // READ, WRITE, SENSITIVE
    private Map<String, Object> inputSchema;
    private boolean requiresHumanConfirmation;

}
