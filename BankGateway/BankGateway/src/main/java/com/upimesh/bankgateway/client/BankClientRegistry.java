package com.upimesh.bankgateway.client;

import com.upimesh.bankgateway.model.enums.BankCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BankClientRegistry {

    private final Map<BankCode, BankClient> clients;

    public BankClientRegistry(List<BankClient> bankClients) {
        this.clients = bankClients.stream()
                .collect(Collectors.toMap(BankClient::getBankCode, client -> client));
        log.info("Registered {} bank clients: {}", clients.size(), 
                clients.keySet().stream().map(Enum::name).collect(Collectors.joining(", ")));
    }

    public Optional<BankClient> getClient(BankCode bankCode) {
        return Optional.ofNullable(clients.get(bankCode));
    }

    public BankClient getClientOrThrow(BankCode bankCode) {
        return getClient(bankCode)
                .orElseThrow(() -> new UnsupportedBankException("Bank is not supported by this gateway: " + 
                        (bankCode != null ? bankCode.getDisplayName() : "UNKNOWN")));
    }

    public BankClient getClientByIfsc(String ifscCode) {
        BankCode bankCode = BankCode.fromIfsc(ifscCode);
        if (bankCode == null) {
            throw new UnsupportedBankException("Unsupported or invalid bank prefix in IFSC: " + ifscCode);
        }
        return getClientOrThrow(bankCode);
    }

    public BankClient getClientByUpiHandle(String upiHandle) {
        if (upiHandle == null || !upiHandle.contains("@")) {
            throw new UnsupportedBankException("Invalid UPI handle format: " + upiHandle);
        }
        String suffix = upiHandle.substring(upiHandle.lastIndexOf("@") + 1).toLowerCase().trim();
        BankCode bankCode = switch (suffix) {
            case "hdfc", "hdfcbank" -> BankCode.HDFC;
            case "sbi", "oksbi" -> BankCode.SBI;
            case "icici", "okicici" -> BankCode.ICICI;
            case "axis", "okaxis" -> BankCode.AXIS;
            case "kotak", "kmbl" -> BankCode.KOTAK;
            case "pnb" -> BankCode.PNB;
            case "yesbank", "yes" -> BankCode.YES;
            case "upimesh", "upi" -> BankCode.HDFC; // default internal maps to HDFC
            default -> throw new UnsupportedBankException("UPI handle suffix '@" + suffix + "' is not supported");
        };

        return getClientOrThrow(bankCode);
    }

    public boolean isSupported(BankCode bankCode) {
        return clients.containsKey(bankCode);
    }

    public Set<BankCode> getSupportedBanks() {
        return clients.keySet();
    }

    // ─── Inner Exception Class ────────────────────────────────────────────────

    public static class UnsupportedBankException extends RuntimeException {
        public UnsupportedBankException(String message) {
            super(message);
        }
    }
}
