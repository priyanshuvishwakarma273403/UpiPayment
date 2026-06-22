package com.upimesh.bankgateway.model.enums;

import lombok.Getter;

/**
 * BankCode — Supported banks in the gateway.
 * Maps to IFSC prefix and specific bank client.
 */
@Getter
public enum BankCode {
    HDFC("HDFC", "HDFC Bank"),
    SBI("SBIN", "State Bank of India"),
    ICICI("ICIC", "ICICI Bank"),
    AXIS("UTIB", "Axis Bank"),
    KOTAK("KKBK", "Kotak Mahindra Bank"),
    PNB("PUNB", "Punjab National Bank"),
    BOB("BARB", "Bank of Baroda"),
    CANARA("CNRB", "Canara Bank"),
    UNION("UBIN", "Union Bank of India"),
    YES("YESB", "Yes Bank"),
    INDUSIND("INDB", "IndusInd Bank"),
    IDFC("IDFB", "IDFC First Bank");

    private final String ifscPrefix;
    private final String displayName;

    BankCode(String ifscPrefix, String displayName) {
        this.ifscPrefix = ifscPrefix;
        this.displayName = displayName;
    }

    // Detect bank from IFSC code (first 4 characters)
    public static BankCode fromIfsc(String ifsc) {
        if (ifsc == null || ifsc.length() < 4) return null;
        String prefix = ifsc.substring(0, 4).toUpperCase();
        for (BankCode code : values()) {
            if (code.ifscPrefix.equals(prefix)) {
                return code;
            }
        }
        return null;
    }
}
