package com.upimesh.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private LocalDateTime aggregatedAt;
    private Long totalTransactions;
    private Long successfulTransactions;
    private Long failedTransactions;
    private BigDecimal totalVolume;
    private BigDecimal platformRevenue;
    private double successRate;
    private Long newUsers;
    private Long activeUsers;
    private BigDecimal avgTransactionValue;
    
    // Geographic Heatmap Representation (Simulated for India regions)
    private List<GeoMetric> geoHeatmap;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GeoMetric implements Serializable {
        private static final long serialVersionUID = 1L;
        private String city;
        private String state;
        private Long transactionCount;
        private BigDecimal volume;
        private double latitude;
        private double longitude;
    }
}
