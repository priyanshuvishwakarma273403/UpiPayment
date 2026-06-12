package com.npci.model.request;

import com.npci.model.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for POST /npci/initiate-transaction
 * This is what Payment Service sends us when user clicks "Pay"
 */
@Data
public class InitiateTransactionRequest {

    @NotBlank(message = "Sender UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z]{2,}$", message = "Invalid UPI ID format")
    private String senderUpiId;         // e.g. 9876543210@upimesh

    @NotBlank(message = "Receiver UPI ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+@[a-zA-Z]{2,}$", message = "Invalid UPI ID format")
    private String receiverUpiId;       // e.g. merchant@hdfc

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum transaction amount is ₹1")
    @DecimalMax(value = "100000.00", message = "Maximum transaction amount is ₹1,00,000")
    private BigDecimal amount;

    @Size(max = 200)
    private String remarks;             // "Payment for Order #1234"

    @NotNull(message = "Transaction type is required")
    private TransactionType type;       // P2P, P2M, etc.

    private String deviceId;
    private String ipAddress;

    // MPIN hash — sent from client, used for NPCI auth (never store raw MPIN!)
    @NotBlank(message = "MPIN hash is required")
    private String mpinHash;

    // Idempotency key — prevents duplicate payments if request retried
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

}
