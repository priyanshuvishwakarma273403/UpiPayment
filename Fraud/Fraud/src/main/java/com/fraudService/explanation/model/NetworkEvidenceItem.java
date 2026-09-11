package com.fraudService.explanation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkEvidenceItem {

    private boolean sharedDeviceDetected;
    private List<String> sharedDevices;

    private boolean sharedIpDetected;
    private List<String> sharedIps;

    private boolean suspiciousBeneficiaryLinkage;
    private List<String> relatedBeneficiaries;

    private double clusterDensityScore;
    private List<String> empiricalEvidenceLines;
}
