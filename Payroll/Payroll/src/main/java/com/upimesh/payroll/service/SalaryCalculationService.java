package com.upimesh.payroll.service;

import com.upimesh.payroll.model.response.DeductionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
public class SalaryCalculationService {

    /**
     * Calculates employee salary deductions:
     * - PF: 12% of basic (basic = 50% of gross, PF only on basic <= ₹15,000)
     * - ESI: 0.75% of gross (only if gross <= ₹21,000/month)
     * - TDS: progressive slab-based projected on annual gross salary (monthly gross * 12)
     */
    public DeductionResult calculateDeductions(BigDecimal grossSalary) {
        if (grossSalary == null || grossSalary.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            return new DeductionResult(zero, zero, zero, zero, zero);
        }

        // 1. PF: 12% of basic (basic = 50% of gross, basic capped at 15000)
        BigDecimal basic = grossSalary.multiply(BigDecimal.valueOf(0.50)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pfBasis = basic.min(BigDecimal.valueOf(15000.00));
        BigDecimal pf = pfBasis.multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP);

        // 2. ESI: 0.75% of gross if monthly gross <= 21000
        BigDecimal esi = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (grossSalary.compareTo(BigDecimal.valueOf(21000.00)) <= 0) {
            esi = grossSalary.multiply(BigDecimal.valueOf(0.0075)).setScale(2, RoundingMode.HALF_UP);
        }

        // 3. TDS: slab-based on annual gross projected (monthly gross * 12)
        BigDecimal annualGross = grossSalary.multiply(BigDecimal.valueOf(12.00));
        double annualGrossVal = annualGross.doubleValue();
        double annualTdsVal = 0.0;

        if (annualGrossVal <= 250000.0) {
            annualTdsVal = 0.0;
        } else if (annualGrossVal <= 500000.0) {
            annualTdsVal = (annualGrossVal - 250000.0) * 0.05;
        } else if (annualGrossVal <= 1000000.0) {
            annualTdsVal = 12500.0 + (annualGrossVal - 500000.0) * 0.20;
        } else {
            annualTdsVal = 12500.0 + 100000.0 + (annualGrossVal - 1000000.0) * 0.30;
        }

        BigDecimal tds = BigDecimal.valueOf(annualTdsVal)
                .divide(BigDecimal.valueOf(12.00), 2, RoundingMode.HALF_UP);

        // 4. Totals computation
        BigDecimal totalDeductions = pf.add(esi).add(tds).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netSalary = grossSalary.subtract(totalDeductions).setScale(2, RoundingMode.HALF_UP);

        log.debug("Deductions calculated for Gross ₹{}: PF=₹{}, ESI=₹{}, TDS=₹{}, Net=₹{}", 
                grossSalary, pf, esi, tds, netSalary);

        return new DeductionResult(pf, esi, tds, totalDeductions, netSalary);
    }
}
