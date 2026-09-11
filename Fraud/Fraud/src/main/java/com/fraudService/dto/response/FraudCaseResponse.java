package com.fraudService.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCaseResponse {

    private String caseId;
    private String customerId;
    private String transactionId;
    private String fraudType;
    private String severity;
    private String status;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String resolution;
    private String resolutionReason;
}
