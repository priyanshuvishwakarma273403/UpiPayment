package com.upimesh.risk.model.dto;

public record DeviceResult(
        boolean isNew,
        double scoreContribution
) {}
