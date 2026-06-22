package com.upimesh.settlement.service;

import com.upimesh.settlement.model.entity.MerchantSettlement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class BankTransferService {

    public String initiateTransfer(MerchantSettlement settlement, String decryptedAccount) {
        log.info("Initiating bank transfer for settlement ID: {} | Mode: {} | Gross: {} | Net: {}", 
                settlement.getSettlementId(), settlement.getMode(), settlement.getGrossAmount(), settlement.getNetAmount());

        String maskedAcc = "XXXX XXXX " + (decryptedAccount.length() > 4 ? 
                decryptedAccount.substring(decryptedAccount.length() - 4) : decryptedAccount);

        switch (settlement.getMode()) {
            case RTGS -> log.info("Executing RTGS transfer to Account: {} | IFSC: {}", maskedAcc, settlement.getMerchantIfsc());
            case NEFT -> log.info("Executing NEFT transfer to Account: {} | IFSC: {}", maskedAcc, settlement.getMerchantIfsc());
            case IMPS -> log.info("Executing IMPS transfer to Account: {} | IFSC: {}", maskedAcc, settlement.getMerchantIfsc());
            case UPI -> log.info("Executing UPI transfer to VPA: {}", settlement.getMerchantUpiId());
        }

        // Generate UTR / Bank Reference Number: "BNK" + 10 chars
        String reference = "BNK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        log.info("Transfer completed successfully. Reference Number: {}", reference);
        return reference;
    }
}
