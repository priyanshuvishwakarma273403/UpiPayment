package com.upimesh.aml.model.dto;

import java.math.BigDecimal;

public record StructuringResult(
        boolean detected,
        BigDecimal total24h,
        int count,
        String pattern
) {}
