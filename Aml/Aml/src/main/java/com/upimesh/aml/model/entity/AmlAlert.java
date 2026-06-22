package com.upimesh.aml.model.entity;

import com.upimesh.aml.model.enums.AlertStatus;
import com.upimesh.aml.model.enums.AlertType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aml_alerts", indexes = {
        @Index(name = "idx_aml_alert_upi_id", columnList = "userUpiId"),
        @Index(name = "idx_aml_alert_type", columnList = "alertType"),
        @Index(name = "idx_aml_alert_status", columnList = "status"),
        @Index(name = "idx_aml_alert_created", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String alertId;

    @Column(nullable = false, length = 50)
    private String transactionId;

    @Column(nullable = false, length = 100)
    private String userUpiId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AlertStatus status = AlertStatus.OPEN;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private double riskScore;

    @Column(length = 100)
    private String assignedTo;

    @Column(length = 500)
    private String resolutionNotes;

    private LocalDateTime escalatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime reportedToFiuAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
