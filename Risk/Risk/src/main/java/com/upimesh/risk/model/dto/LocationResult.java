package com.upimesh.risk.model.dto;

public record LocationResult(
        boolean isAnomaly,
        double scoreContribution,
        String reason
) {}
