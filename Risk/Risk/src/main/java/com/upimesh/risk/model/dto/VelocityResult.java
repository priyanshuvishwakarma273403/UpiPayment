package com.upimesh.risk.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Result DTO containing high-speed velocity counts across dimensions and sliding time windows.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VelocityResult {

    private long customerCount1m;
    private long customerCount5m;
    private long customerCount1h;

    private long deviceCount1m;
    private long deviceCount1h;

    private long ipCount1m;
    private long ipCount1h;

    private long beneficiaryCount1m;
    private long beneficiaryCount1h;

    // Flag indicating whether Redis was down/unreachable during evaluation
    private boolean degraded;
    private boolean fallbackApplied;
    private String fallbackReason;

    @Builder.Default
    private Map<String, Object> details = new HashMap<>();
}
