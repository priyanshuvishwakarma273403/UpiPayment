package com.aiService.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Chat Response DTO */
@Data
@Builder
public class ChatResponse {
    private String sessionId;
    private String userMessage;
    private String aiResponse;
    private String model;
    private Long userId;
    private LocalDateTime timestamp;
}