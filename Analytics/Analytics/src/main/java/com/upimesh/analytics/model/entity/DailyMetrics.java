package com.upimesh.analytics.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_metrics", indexes = {
        @Index(name = "idx_daily_metrics_date", columnList = "date", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricsId;

    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "total_transactions", nullable = false)
    private Long totalTransactions;

    @Column(name = "successful_transactions", nullable = false)
    private Long successfulTransactions;

    @Column(name = "failed_transactions", nullable = false)
    private Long failedTransactions;

    @Column(name = "total_volume", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalVolume;

    @Column(name = "platform_revenue", nullable = false, precision = 15, scale = 2)
    private BigDecimal platformRevenue;

    @Column(name = "new_users", nullable = false)
    private Long newUsers;

    @Column(name = "active_users", nullable = false)
    private Long activeUsers;

    @Column(name = "avg_transaction_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal avgTransactionValue;

    @Column(name = "success_rate", nullable = false)
    private double successRate;

    @Column(name = "top_merchant")
    private String topMerchant;

    @Column(name = "peak_hour", nullable = false)
    private int peakHour;
}
