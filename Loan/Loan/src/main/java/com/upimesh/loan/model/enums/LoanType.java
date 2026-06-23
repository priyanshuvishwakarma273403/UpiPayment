package com.upimesh.loan.model.enums;

public enum LoanType {
    BNPL_30DAY(1, true),
    BNPL_90DAY(3, true),
    PERSONAL_3MONTH(3, false),
    PERSONAL_6MONTH(6, false),
    PERSONAL_12MONTH(12, false);

    private final int tenureMonths;
    private final boolean isBnpl;

    LoanType(int tenureMonths, boolean isBnpl) {
        this.tenureMonths = tenureMonths;
        this.isBnpl = isBnpl;
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public boolean isBnpl() {
        return isBnpl;
    }
}
