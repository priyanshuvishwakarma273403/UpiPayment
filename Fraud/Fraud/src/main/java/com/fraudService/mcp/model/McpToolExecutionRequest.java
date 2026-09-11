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
public class McpToolExecutionRequest {

    private String toolName;
    private Map<String, Object> arguments;
    private String actor;
    private String correlationId;
    private boolean confirmedByHuman;
    private String humanAnalystId;

}
