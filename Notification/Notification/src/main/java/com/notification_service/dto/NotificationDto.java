package com.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * NotificationDto - Kafka event parse hone ke baad banta hai
 * Email + SMS dono service isko use karti hain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private String     paymentId;
    private Long       senderId;
    private Long       receiverId;
    private String     senderUpiId;
    private String     receiverUpiId;

    // Contact info (production mein user-service se fetch hoga)
    private String     senderEmail;
    private String     receiverEmail;
    private String     senderPhone;
    private String     receiverPhone;
    private String     senderName;
    private String     receiverName;

    private BigDecimal amount;
    private String     paymentStatus;
    private String     paymentMode;
    private String     description;
    private String     failureReason;
    private LocalDateTime timestamp;

    public enum NotificationType { EMAIL, SMS, BOTH }
    private NotificationType notificationType;

}
