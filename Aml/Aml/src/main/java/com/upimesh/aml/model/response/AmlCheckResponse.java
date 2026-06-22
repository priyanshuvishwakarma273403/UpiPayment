package com.upimesh.aml.model.response;

import com.upimesh.aml.model.enums.AmlRiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlCheckResponse {
    private String transactionId;
    private String screeningId;
    private AmlRiskLevel riskLevel;
    private double riskScore;
    private boolean allowed;
    private String blockReason;
    private List<String> flagsTriggered;
    private LocalDateTime screenedAt;
}
