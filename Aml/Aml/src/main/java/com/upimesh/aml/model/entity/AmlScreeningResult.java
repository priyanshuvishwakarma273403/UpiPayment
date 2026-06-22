package com.upimesh.aml.model.entity;

import com.upimesh.aml.model.converter.ListToJsonConverter;
import com.upimesh.aml.model.enums.AmlRiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "aml_screening_results", indexes = {
        @Index(name = "idx_aml_screen_txn_id", columnList = "transactionId"),
        @Index(name = "idx_aml_screen_upi_id", columnList = "userUpiId"),
        @Index(name = "idx_aml_screen_date", columnList = "screenedAt"),
        @Index(name = "idx_aml_screen_risk", columnList = "riskLevel")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlScreeningResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String screeningId;

    @Column(nullable = false, length = 50)
    private String transactionId;

    @Column(nullable = false, length = 100)
    private String userUpiId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AmlRiskLevel riskLevel;

    @Column(nullable = false)
    private double riskScore;

    @Column(nullable = false, precision = 18, scale = 2)
    private java.math.BigDecimal amount;

    @Column(nullable = false)
    private boolean blocked;

    @Column(length = 200)
    private String blockReason;

    @Convert(converter = ListToJsonConverter.class)
    @Column(length = 500)
    private List<String> checksPerformed;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime screenedAt;
}
