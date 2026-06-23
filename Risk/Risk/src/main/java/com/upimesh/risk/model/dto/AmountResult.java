package com.upimesh.risk.model.dto;

public record AmountResult(
        boolean isUnusual,
        double scoreContribution
) {}
