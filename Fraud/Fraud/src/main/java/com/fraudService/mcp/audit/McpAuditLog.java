package com.fraudService.mcp.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpAuditLog {

    private String auditId;
    private String correlationId;
    private String actor;
    private String tool;
    private LocalDateTime timestamp;
    private String argumentsJson;
    private String resultJson;
    private String status;
    private boolean humanConfirmed;

}
