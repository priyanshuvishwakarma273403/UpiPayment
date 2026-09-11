package com.fraudService.network.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudClusterReport {

    private String clusterId;
    private String title; // "Potential coordinated fraud network detected"
    private String severity;
    private Double clusterRiskScore;
    private int totalNodesCount;
    private int totalRelationshipsCount;
    private List<String> sharedDevices;
    private List<String> sharedIps;
    private List<String> sharedBeneficiaries;
    private List<String> empiricalEvidence;
    private LocalDateTime detectedAt;
    private NetworkGraph networkGraph;
}
