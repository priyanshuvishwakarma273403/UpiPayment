package com.upimesh.analytics.service;

import com.upimesh.analytics.dto.DashboardSummaryDto;
import com.upimesh.analytics.dto.TransactionResponseDto;
import com.upimesh.analytics.feign.TransactionServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final TransactionServiceClient transactionServiceClient;

    public DashboardSummaryDto getDashboardSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusHours(24);

        List<TransactionResponseDto> txns = Collections.emptyList();
        try {
            txns = transactionServiceClient.getDebitTransactionsBetween(from, now);
        } catch (Exception e) {
            log.error("Failed to fetch transactions from transaction-service: {}", e.getMessage());
        }

        long total = txns.size();
        long success = txns.stream().filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus())).count();
        long failed = total - success;

        BigDecimal volume = txns.stream()
                .map(TransactionResponseDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal revenue = volume.multiply(new BigDecimal("0.001")).setScale(2, RoundingMode.HALF_UP);
        double successRate = total > 0 ? ((double) success / total) * 100.0 : 100.0;
        BigDecimal avgValue = total > 0 ? volume.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        List<DashboardSummaryDto.GeoMetric> geo = List.of(
                new DashboardSummaryDto.GeoMetric("Mumbai", "Maharashtra", total / 3 + 1, volume.multiply(new BigDecimal("0.4")), 19.0760, 72.8777),
                new DashboardSummaryDto.GeoMetric("Bengaluru", "Karnataka", total / 3, volume.multiply(new BigDecimal("0.35")), 12.9716, 77.5946),
                new DashboardSummaryDto.GeoMetric("Delhi", "Delhi", total / 3, volume.multiply(new BigDecimal("0.25")), 28.7041, 77.1025)
        );

        return DashboardSummaryDto.builder()
                .aggregatedAt(now)
                .totalTransactions(total)
                .successfulTransactions(success)
                .failedTransactions(failed)
                .totalVolume(volume)
                .platformRevenue(revenue)
                .successRate(successRate)
                .newUsers(15L)
                .activeUsers(total + 5)
                .avgTransactionValue(avgValue)
                .geoHeatmap(geo)
                .build();
    }
}
