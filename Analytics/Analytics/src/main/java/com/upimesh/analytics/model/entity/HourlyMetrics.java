package com.upimesh.analytics.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "hourly_metrics", indexes = {
        @Index(name = "idx_hourly_metrics_date_hour", columnList = "date, hour")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlyMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "hour", nullable = false)
    private int hour;

    @Column(name = "transaction_count", nullable = false)
    private Long transactionCount;

    @Column(name = "volume", nullable = false, precision = 15, scale = 2)
    private BigDecimal volume;

    @Column(name = "success_rate", nullable = false)
    private double successRate;
}
