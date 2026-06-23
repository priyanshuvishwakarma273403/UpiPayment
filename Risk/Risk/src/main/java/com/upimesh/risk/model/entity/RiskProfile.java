package com.upimesh.risk.model.entity;

import com.upimesh.risk.model.converter.JsonListConverter;
import com.upimesh.risk.model.converter.JsonSetConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "risk_profiles", indexes = {
        @Index(name = "idx_risk_profile_user_id", columnList = "userId", unique = true),
        @Index(name = "idx_risk_profile_user_upi_id", columnList = "userUpiId", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String userId;

    @Column(nullable = false, length = 100, unique = true)
    private String userUpiId;

    @Builder.Default
    @Column(nullable = false)
    private double baseRiskScore = 0.1;

    @Column(nullable = false)
    private int totalTransactions;

    @Column(nullable = false)
    private int successfulTransactions;

    @Column(nullable = false)
    private int failedTransactions;

    @Column(precision = 18, scale = 2)
    private BigDecimal avgTransactionAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal maxTransactionAmount;

    @Convert(converter = JsonListConverter.class)
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private List<Integer> typicalTransactionHours = new ArrayList<>();

    @Convert(converter = JsonSetConverter.class)
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private Set<String> knownDevices = new HashSet<>();

    @Convert(converter = JsonSetConverter.class)
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private Set<String> usualCities = new HashSet<>();

    private LocalDateTime lastTransactionAt;

    private LocalDateTime profileUpdatedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
