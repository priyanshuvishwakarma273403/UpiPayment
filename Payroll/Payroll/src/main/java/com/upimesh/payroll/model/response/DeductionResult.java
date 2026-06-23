package com.upimesh.payroll.model.response;

import java.math.BigDecimal;

public record DeductionResult(
        BigDecimal pf,
        BigDecimal esi,
        BigDecimal tds,
        BigDecimal totalDeductions,
        BigDecimal netSalary
) {}
