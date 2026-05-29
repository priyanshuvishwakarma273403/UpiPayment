package com.aiService.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Chat Request DTO */
@Data
public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    private String message;

    private String sessionId;   // Existing session continue karne ke liye (optional)
}

