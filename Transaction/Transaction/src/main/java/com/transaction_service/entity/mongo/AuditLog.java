package com.transaction_service.entity.mongo;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * AuditLog - MongoDB Collection: audit_logs
 * System-wide audit trail - kaunsa user ne kya action liya.
 */
@Document(collection = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    private String id;

    @Indexed
    private Long userId;

    private String action;       // PAYMENT_INITIATED, PAYMENT_COMPLETED, etc.
    private String entityType;   // PAYMENT, WALLET, USER
    private String entityId;     // paymentId, walletId, userId

    private String details;      // JSON string of changed fields
    private String ipAddress;
    private String userAgent;

    @Indexed
    private String serviceName;  // Which microservice generated this

    @Indexed
    private LocalDateTime timestamp;

}
