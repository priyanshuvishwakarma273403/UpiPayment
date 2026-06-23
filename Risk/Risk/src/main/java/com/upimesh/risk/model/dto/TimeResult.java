package com.upimesh.risk.model.dto;

public record TimeResult(
        boolean isUnusual,
        double scoreContribution
) {}
