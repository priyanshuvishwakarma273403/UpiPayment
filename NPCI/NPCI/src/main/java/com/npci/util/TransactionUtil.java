package com.npci.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * TransactionUtil — Static helpers for transaction processing.
 * All methods are stateless — no Spring bean needed.
 */
@UtilityClass
public class TransactionUtil {

    private static final DateTimeFormatter TXN_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ─── ID Generation ────────────────────────────────────────────────────────

    /**
     * Generate unique transaction ID.
     * Format: TXN + timestamp + random (e.g. TXN20240115143022A1B2C3D4)
     */
    public static String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(TXN_FORMAT);
        String random = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8).toUpperCase();
        return "TXN" + timestamp + random;
    }

    /**
     * Generate unique refund ID.
     * Format: RFND + timestamp + random
     */
    public static String generateRefundId() {
        String timestamp = LocalDateTime.now().format(TXN_FORMAT);
        String random = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 6).toUpperCase();
        return "RFND" + timestamp + random;
    }

    /**
     * Generate mandate ID.
     * Format: MND + timestamp + random
     */
    public static String generateMandateId() {
        String timestamp = LocalDateTime.now().format(TXN_FORMAT);
        String random = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 6).toUpperCase();
        return "MND" + timestamp + random;
    }

    // ─── UPI ID Utilities ─────────────────────────────────────────────────────

    /**
     * Validate UPI ID format: localpart@bankhandle
     * Valid: 9876543210@upimesh, merchant.name@hdfc, user-name@oksbi
     */
    public static boolean isValidUpiId(String upiId) {
        if (upiId == null || upiId.isBlank()) return false;
        return upiId.matches("^[a-zA-Z0-9._\\-+]+@[a-zA-Z]{2,}$");
    }

    /**
     * Extract bank handle from UPI ID.
     * "9876543210@upimesh" → "upimesh"
     */
    public static String extractBankHandle(String upiId) {
        if (upiId == null || !upiId.contains("@")) return "";
        return upiId.substring(upiId.indexOf('@') + 1).toLowerCase();
    }

    /**
     * Extract local part from UPI ID.
     * "9876543210@upimesh" → "9876543210"
     */
    public static String extractLocalPart(String upiId) {
        if (upiId == null || !upiId.contains("@")) return upiId;
        return upiId.substring(0, upiId.indexOf('@'));
    }

    // ─── NPCI Response Code Mapping ───────────────────────────────────────────

    /**
     * Human-readable message for NPCI response codes.
     * Banks use ISO 8583 standard codes + NPCI-specific extensions.
     */
    public static String getNpciResponseMessage(String responseCode) {
        if (responseCode == null) return "Unknown error";

        return switch (responseCode) {
            case "00"  -> "Transaction successful";
            case "01"  -> "Refer to card issuer";
            case "05"  -> "Do not honour — bank declined";
            case "14"  -> "Invalid account number";
            case "51"  -> "Insufficient funds";
            case "54"  -> "Expired card/account";
            case "55"  -> "Incorrect PIN/MPIN";
            case "57"  -> "Transaction not permitted to cardholder";
            case "61"  -> "Transaction amount limit exceeded";
            case "65"  -> "Daily transaction count limit exceeded";
            case "91"  -> "Issuer or switch is inoperative";
            case "96"  -> "System malfunction";
            case "Z1"  -> "Blacklisted customer";
            case "Z9"  -> "Insufficient funds in linked account";
            case "ZA"  -> "Daily limit exceeded";
            case "ZB"  -> "Transaction amount limit exceeded";
            case "ZC"  -> "Invalid UPI PIN";
            case "ZD"  -> "UPI transaction not permitted";
            case "ZM"  -> "Invalid MPIN";
            case "ZN"  -> "MPIN not set";
            case "ZP"  -> "Collect request expired";
            case "ZR"  -> "Debit freeze on account";
            case "ZS"  -> "Credit freeze on account";
            case "BT"  -> "Request timeout";
            case "U16" -> "Risk threshold exceeded — transaction blocked by fraud system";
            case "U30" -> "Beneficiary bank is down";
            case "U69" -> "Request timed out — please check status before retrying";
            case "XB"  -> "Bank servers under maintenance";
            default    -> "Transaction failed (code: " + responseCode + ")";
        };
    }

    /**
     * Is this response code a terminal failure (no point retrying)?
     */
    public static boolean isTerminalFailure(String responseCode) {
        return switch (responseCode) {
            case "14",  // Invalid account
                 "51",  // Insufficient funds
                 "55",  // Wrong MPIN
                 "57",  // Not permitted
                 "Z1",  // Blacklisted
                 "Z9",  // Insufficient funds
                 "ZC",  // Invalid UPI PIN
                 "ZM",  // Invalid MPIN
                 "ZN",  // MPIN not set
                 "ZR",  // Debit freeze
                 "ZS"   // Credit freeze
                    -> true;
            default -> false;
        };
    }

    /**
     * Is this response code retryable?
     */
    public static boolean isRetryable(String responseCode) {
        return switch (responseCode) {
            case "91",  // Bank down
                 "96",  // System malfunction
                 "U30", // Beneficiary bank down
                 "U69", // Timeout
                 "BT",  // Timeout
                 "XB"   // Maintenance
                    -> true;
            default -> false;
        };
    }

    // ─── Amount Utilities ─────────────────────────────────────────────────────

    /**
     * Convert rupees to paise (some banks use paise).
     * ₹100.50 → 10050
     */
    public static long rupeesToPaise(java.math.BigDecimal rupees) {
        return rupees.multiply(new java.math.BigDecimal("100")).longValue();
    }

    /**
     * Convert paise to rupees.
     * 10050 → ₹100.50
     */
    public static java.math.BigDecimal paiseToRupees(long paise) {
        return new java.math.BigDecimal(paise).divide(new java.math.BigDecimal("100"));
    }

    /**
     * Format amount for display: 1000.5 → "₹1,000.50"
     */
    public static String formatAmount(java.math.BigDecimal amount) {
        return "₹" + String.format("%,.2f", amount);
    }
}
