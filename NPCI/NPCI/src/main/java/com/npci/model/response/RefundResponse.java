package com.npci.model.response;

import com.npci.model.enums.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response after refund request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {

    private String refundId;
    private String originalTransactionId;
    private BigDecimal refundAmount;
    private String refundToUpiId;
    private RefundStatus status;
    private String reason;
    private LocalDateTime expectedCreditBy;
    private LocalDateTime createdAt;

}
