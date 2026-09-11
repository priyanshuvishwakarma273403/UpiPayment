package com.fraudService.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaseRequest {

    @NotBlank(message = "customerId is required")
    private String customerId;

    @NotBlank(message = "transactionId is required")
    private String transactionId;

    @NotBlank(message = "fraudType is required")
    private String fraudType;

    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    private String assignedTo;

    private String initialComment;
}
