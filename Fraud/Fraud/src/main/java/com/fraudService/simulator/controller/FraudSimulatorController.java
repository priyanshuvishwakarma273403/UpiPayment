package com.fraudService.simulator.controller;

import com.fraudService.simulator.model.FraudSimulationRequest;
import com.fraudService.simulator.model.FraudSimulationResponse;
import com.fraudService.simulator.service.FraudSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Phase 19 Fraud Attack Simulator.
 * Provides analyst endpoints for running attack vector simulations and fetching presets.
 */
@RestController
@RequestMapping("/fraud/simulator")
@RequiredArgsConstructor
@Slf4j
public class FraudSimulatorController {

    private final FraudSimulationService simulationService;

    @PostMapping("/run")
    public ResponseEntity<FraudSimulationResponse> runSimulation(@RequestBody FraudSimulationRequest request) {
        log.info("Received request to run fraud attack simulation for customerId={}", request.getSimulatedCustomerId());
        FraudSimulationResponse response = simulationService.runSimulation(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/presets")
    public ResponseEntity<List<Map<String, Object>>> getSimulationPresets() {
        List<Map<String, Object>> presets = List.of(
                Map.of(
                        "id", "ACCOUNT_TAKEOVER",
                        "name", "Account Takeover (ATO) Attack",
                        "description", "Simulates unrecognized new device switch followed by high value transfer",
                        "request", FraudSimulationRequest.builder()
                                .transactionAmount(new BigDecimal("85000"))
                                .accountAgeDays(180.0)
                                .deviceAgeDays(0.2)
                                .beneficiaryAgeDays(30.0)
                                .velocity(1.5)
                                .locationDeviation(125.0)
                                .ipRiskScore(0.85)
                                .merchantRiskScore(0.30)
                                .simulatedCustomerId("CUS_ATO_01")
                                .simulatedDeviceId("DEV_NEW_99")
                                .simulatedIpAddress("185.220.101.5")
                                .build()
                ),
                Map.of(
                        "id", "VELOCITY_BURST",
                        "name", "Rapid Drain Velocity Burst",
                        "description", "Simulates automated bot burst draining funds to new beneficiary",
                        "request", FraudSimulationRequest.builder()
                                .transactionAmount(new BigDecimal("45000"))
                                .accountAgeDays(45.0)
                                .deviceAgeDays(15.0)
                                .beneficiaryAgeDays(0.05)
                                .velocity(14.0)
                                .locationDeviation(15.0)
                                .ipRiskScore(0.65)
                                .merchantRiskScore(0.40)
                                .simulatedCustomerId("CUS_VEL_02")
                                .simulatedDeviceId("DEV_VEL_12")
                                .simulatedIpAddress("198.51.100.42")
                                .build()
                ),
                Map.of(
                        "id", "HIGH_RISK_MERCHANT_FRAUD",
                        "name", "High-Risk Gaming / Crypto Merchant Scams",
                        "description", "Simulates payment routing to known high-risk fraudulent merchant endpoint",
                        "request", FraudSimulationRequest.builder()
                                .transactionAmount(new BigDecimal("60000"))
                                .accountAgeDays(90.0)
                                .deviceAgeDays(60.0)
                                .beneficiaryAgeDays(2.0)
                                .velocity(3.0)
                                .locationDeviation(5.0)
                                .ipRiskScore(0.70)
                                .merchantRiskScore(0.92)
                                .simulatedCustomerId("CUS_MERCH_03")
                                .simulatedDeviceId("DEV_STD_44")
                                .simulatedIpAddress("203.0.113.88")
                                .build()
                ),
                Map.of(
                        "id", "FRESH_ACCOUNT_MULE",
                        "name", "Fresh Mule Account Drain",
                        "description", "Simulates brand new account receiving and dispersing funds immediately",
                        "request", FraudSimulationRequest.builder()
                                .transactionAmount(new BigDecimal("95000"))
                                .accountAgeDays(1.5)
                                .deviceAgeDays(0.5)
                                .beneficiaryAgeDays(0.1)
                                .velocity(9.0)
                                .locationDeviation(80.0)
                                .ipRiskScore(0.75)
                                .merchantRiskScore(0.50)
                                .simulatedCustomerId("CUS_MULE_04")
                                .simulatedDeviceId("DEV_MULE_01")
                                .simulatedIpAddress("198.51.100.99")
                                .build()
                ),
                Map.of(
                        "id", "NORMAL_BENIGN",
                        "name", "Standard Benign Transaction",
                        "description", "Simulates normal everyday merchant payment from trusted device",
                        "request", FraudSimulationRequest.builder()
                                .transactionAmount(new BigDecimal("1200"))
                                .accountAgeDays(365.0)
                                .deviceAgeDays(180.0)
                                .beneficiaryAgeDays(120.0)
                                .velocity(1.0)
                                .locationDeviation(2.0)
                                .ipRiskScore(0.05)
                                .merchantRiskScore(0.10)
                                .simulatedCustomerId("CUS_SAFE_05")
                                .simulatedDeviceId("DEV_TRUSTED_01")
                                .simulatedIpAddress("103.21.12.44")
                                .build()
                )
        );
        return ResponseEntity.ok(presets);
    }
}
