package com.transaction_service.entity.mongo;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * PaymentLog - MongoDB Collection: payment_logs
 * Complete payment payload ka raw log.
 * Fast write, schema-flexible.
 */
@Document(collection = "payment_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentLog {

    @Id
    private String id;

    @Indexed
    private String paymentId;

    @Indexed
    private Long senderId;

    @Indexed
    private Long receiverId;

    private String senderUpiId;
    private String receiverUpiId;
    private BigDecimal amount;
    private String paymentStatus;
    private String paymentMode;
    private String fraudStatus;
    private Double fraudScore;
    private String description;
    private Map<String, Object> metadata; // Extra fields (device, IP, etc.)

    @Indexed
    private LocalDateTime paymentTimestamp;
    private LocalDateTime loggedAt;

}
