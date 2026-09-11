package com.fraudService.network.controller;

import com.fraudService.network.model.FraudClusterReport;
import com.fraudService.network.model.NetworkGraph;
import com.fraudService.network.service.FraudNetworkIntelligenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fraud/network")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fraud Network Intelligence", description = "Graph intelligence and network cluster APIs")
public class FraudNetworkController {

    private final FraudNetworkIntelligenceService networkIntelligenceService;

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get bounded graph network neighborhood for a customer")
    public ResponseEntity<NetworkGraph> getCustomerNetwork(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "2") Integer depth) {
        log.info("REST: Query customer network for customerId={} with depth={}", customerId, depth);
        return ResponseEntity.ok(networkIntelligenceService.getCustomerNetwork(customerId, depth));
    }

    @GetMapping("/transaction/{transactionId}")
    @Operation(summary = "Get bounded graph neighborhood for a transaction")
    public ResponseEntity<NetworkGraph> getTransactionNeighborhood(
            @PathVariable String transactionId,
            @RequestParam(defaultValue = "2") Integer depth) {
        log.info("REST: Query transaction neighborhood for transactionId={} with depth={}", transactionId, depth);
        return ResponseEntity.ok(networkIntelligenceService.getTransactionNeighborhood(transactionId, depth));
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get bounded graph network for a device")
    public ResponseEntity<NetworkGraph> getDeviceNetwork(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "2") Integer depth) {
        log.info("REST: Query device network for deviceId={} with depth={}", deviceId, depth);
        return ResponseEntity.ok(networkIntelligenceService.getDeviceNetwork(deviceId, depth));
    }

    @GetMapping("/beneficiary/{beneficiaryUpiId}")
    @Operation(summary = "Get bounded graph network for a beneficiary")
    public ResponseEntity<NetworkGraph> getBeneficiaryNetwork(
            @PathVariable String beneficiaryUpiId,
            @RequestParam(defaultValue = "2") Integer depth) {
        log.info("REST: Query beneficiary network for beneficiaryUpiId={} with depth={}", beneficiaryUpiId, depth);
        return ResponseEntity.ok(networkIntelligenceService.getBeneficiaryNetwork(beneficiaryUpiId, depth));
    }

    @GetMapping("/clusters")
    @Operation(summary = "Detect suspicious fraud network clusters and coordinated fraud rings")
    public ResponseEntity<List<FraudClusterReport>> getSuspiciousClusters() {
        log.info("REST: Run fraud cluster and network ring detection");
        return ResponseEntity.ok(networkIntelligenceService.getSuspiciousClusters());
    }
}
