package com.upimesh.risk.model.entity;

import com.upimesh.risk.model.converter.JsonRiskFactorListConverter;
import com.upimesh.risk.model.enums.RiskLevel;
import com.upimesh.risk.model.enums.RiskFactor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "risk_scoring_results", indexes = {
        @Index(name = "idx_risk_score_txn_id", columnList = "transactionId"),
        @Index(name = "idx_risk_score_upi_id", columnList = "userUpiId"),
        @Index(name = "idx_risk_score_date", columnList = "scoredAt"),
        @Index(name = "idx_risk_score_level", columnList = "riskLevel")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskScoringResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String scoringId;

    @Column(nullable = false, length = 50)
    private String transactionId;

    @Column(nullable = false, length = 100)
    private String userId;

    @Column(nullable = false, length = 100)
    private String userUpiId;

    @Column(nullable = false)
    private double finalScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Convert(converter = JsonRiskFactorListConverter.class)
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private List<RiskFactor> factorsTriggered = new ArrayList<>();

    @Column(length = 100)
    private String deviceId;

    @Column(length = 50)
    private String ipAddress;

    @Column(nullable = false)
    private int hour;

    @Column(nullable = false)
    private int dayOfWeek;

    @Column(nullable = false)
    private boolean isNewDevice;

    @Column(nullable = false)
    private boolean locationAnomaly;

    @Column(nullable = false)
    private boolean velocityHigh;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime scoredAt;
}
