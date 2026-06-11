package com.npci.model.response;

import com.npci.model.enums.TransactionStatus;
import com.npci.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response after initiating a transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String transactionId;           // Our internal ID
    private String npciTransactionId;       // NPCI's ID (for bank disputes)
    private String rrn;                     // Retrieval Reference Number
    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private TransactionStatus status;
    private TransactionType type;
    private String remarks;
    private String npciResponseCode;
    private String npciResponseMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

}
