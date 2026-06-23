package com.upimesh.dispute.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    private String originalTransactionId;
    private BigDecimal refundAmount;
    private String reason;
    private String initiatedBy;
}
