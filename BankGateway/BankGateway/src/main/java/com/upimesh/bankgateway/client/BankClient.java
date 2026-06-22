package com.upimesh.bankgateway.client;

import com.upimesh.bankgateway.model.enums.BankCode;

import java.util.Map;

/**
 * BankClient — Strategy pattern interface for bank connections.
 */
public interface BankClient {

    BankCode getBankCode();

    /**
     * Verifies the bank account exists and gets basic details.
     * Returns keys: holderName, accountType, active, referenceToken, maskedAccount
     */
    Map<String, String> verifyAccount(String accountNumber, String ifscCode);

    /**
     * Fetches the balance details.
     * Returns keys: available, ledger, currency
     */
    Map<String, Object> fetchBalance(String accountNumber, String ifscCode, String bankReferenceToken);

    /**
     * Resolves which customer owns this UPI handle/VPA.
     * Returns keys: holderName, maskedAccount, ifscCode, active
     */
    Map<String, String> resolveUpiHandle(String upiHandle);

    /**
     * Links a bank account to a user's UPI handle in the bank system.
     * Returns bankReferenceToken
     */
    String linkAccount(String accountNumber, String ifscCode, String userUpiId);
}
