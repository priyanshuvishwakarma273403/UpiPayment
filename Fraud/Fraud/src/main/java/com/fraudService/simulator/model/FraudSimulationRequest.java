package com.fraudService.simulator.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Analyst simulation input parameters for Fraud Attack Simulator.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudSimulationRequest {

    private BigDecimal transactionAmount;
    private Double accountAgeDays;
    private Double deviceAgeDays;
    private Double beneficiaryAgeDays;
    private Double velocity;
    private Double locationDeviation;
    private Double ipRiskScore;
    private Double merchantRiskScore;

    @Builder.Default
    private String simulatedCustomerId = "CUS_SIM_9999";

    @Builder.Default
    private String simulatedDeviceId = "DEV_SIM_7777";

    @Builder.Default
    private String simulatedIpAddress = "192.168.1.100";

    @Builder.Default
    private String simulatedMerchantId = "MERCH_SIM_4444";
}
