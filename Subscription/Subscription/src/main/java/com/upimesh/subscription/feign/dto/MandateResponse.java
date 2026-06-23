package com.upimesh.subscription.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MandateResponse {
    private String mandateId;
    private String npciMandateId;
    private String userUpiId;
    private String merchantUpiId;
    private String merchantName;
    private BigDecimal maxAmount;
    private String frequency;
    private Integer executionDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextExecutionDate;
    private MandateStatus status;
    private String purpose;
    private LocalDateTime createdAt;
}
