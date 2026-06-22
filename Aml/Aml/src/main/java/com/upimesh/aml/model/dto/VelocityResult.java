package com.upimesh.aml.model.dto;

public record VelocityResult(
        boolean exceeded,
        String reason,
        int hourlyCount,
        int dailyCount
) {}
