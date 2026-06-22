package com.upimesh.bankgateway.service;

import com.upimesh.bankgateway.client.BankClient;
import com.upimesh.bankgateway.client.BankClientRegistry;
import com.upimesh.bankgateway.exception.UpiHandleNotFoundException;
import com.upimesh.bankgateway.model.entity.UpiHandleResolution;
import com.upimesh.bankgateway.model.enums.VerificationStatus;
import com.upimesh.bankgateway.model.response.UpiResolveResponse;
import com.upimesh.bankgateway.repository.UpiHandleResolutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpiResolveService {

    private final UpiHandleResolutionRepository upiHandleRepo;
    private final BankClientRegistry registry;

    public UpiResolveResponse resolveUpiHandle(String upiHandle) {
        if (upiHandle == null || upiHandle.isBlank()) {
            throw new IllegalArgumentException("UPI handle cannot be blank");
        }

        String targetHandle = upiHandle.trim();
        log.info("Resolving UPI handle: {}", targetHandle);

        // 1. Check DB Cache (1 hour TTL)
        var optionalResolution = upiHandleRepo.findByUpiHandle(targetHandle);
        if (optionalResolution.isPresent()) {
            UpiHandleResolution res = optionalResolution.get();
            if (res.getLastResolvedAt() != null && res.getLastResolvedAt().isAfter(LocalDateTime.now().minusHours(1))) {
                log.info("Returning fresh cached UPI resolution from DB: {}", targetHandle);
                return mapToResponse(res, true);
            }
        }

        // 2. Query Bank Client
        BankClient bankClient = registry.getClientByUpiHandle(targetHandle);
        Map<String, String> response = bankClient.resolveUpiHandle(targetHandle);

        if (response == null || response.containsKey("error") || !"true".equals(response.get("active"))) {
            log.warn("UPI handle resolution failed for {}: {}", targetHandle, response != null ? response.get("error") : "empty response");
            throw new UpiHandleNotFoundException("UPI handle not found or inactive: " + targetHandle);
        }

        // 3. Save/Update in DB
        UpiHandleResolution entity = optionalResolution.orElseGet(() -> UpiHandleResolution.builder().upiHandle(targetHandle).build());
        entity.setBankCode(bankClient.getBankCode());
        entity.setAccountHolderName(response.get("holderName"));
        entity.setMaskedAccountNumber(response.get("maskedAccount"));
        entity.setIfscCode(response.get("ifscCode"));
        entity.setVerificationStatus(VerificationStatus.VERIFIED);
        entity.setIsActive(true);
        entity.setLastResolvedAt(LocalDateTime.now());

        entity = upiHandleRepo.save(entity);
        log.info("Saved UPI resolution to cache: {}", targetHandle);

        return mapToResponse(entity, false);
    }

    @Scheduled(fixedDelay = 3600000) // Run every 1 hour
    public void refreshStaleResolutions() {
        log.info("Scheduled task: Refreshing stale UPI handle resolutions");
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        List<UpiHandleResolution> staleResolutions = upiHandleRepo.findStaleResolutions(cutoff);

        log.info("Found {} stale resolutions to refresh", staleResolutions.size());
        for (UpiHandleResolution resolution : staleResolutions) {
            try {
                log.debug("Refreshing stale resolution: {}", resolution.getUpiHandle());
                BankClient bankClient = registry.getClientByUpiHandle(resolution.getUpiHandle());
                Map<String, String> response = bankClient.resolveUpiHandle(resolution.getUpiHandle());

                if (response == null || response.containsKey("error") || !"true".equals(response.get("active"))) {
                    log.warn("Stale resolution for {} is now inactive or not found. Deactivating.", resolution.getUpiHandle());
                    resolution.setIsActive(false);
                    resolution.setVerificationStatus(VerificationStatus.FAILED);
                } else {
                    resolution.setAccountHolderName(response.get("holderName"));
                    resolution.setMaskedAccountNumber(response.get("maskedAccount"));
                    resolution.setIfscCode(response.get("ifscCode"));
                    resolution.setVerificationStatus(VerificationStatus.VERIFIED);
                    resolution.setIsActive(true);
                }
                resolution.setLastResolvedAt(LocalDateTime.now());
                upiHandleRepo.save(resolution);
            } catch (Exception e) {
                log.error("Failed to refresh stale UPI handle resolution: {}", resolution.getUpiHandle(), e);
            }
        }
    }

    private UpiResolveResponse mapToResponse(UpiHandleResolution entity, boolean fromCache) {
        return UpiResolveResponse.builder()
                .upiHandle(entity.getUpiHandle())
                .accountHolderName(entity.getAccountHolderName())
                .bankCode(entity.getBankCode())
                .bankName(entity.getBankCode().getDisplayName())
                .maskedAccountNumber(entity.getMaskedAccountNumber())
                .ifscCode(entity.getIfscCode())
                .verificationStatus(entity.getVerificationStatus())
                .isActive(entity.isActive())
                .lastResolvedAt(entity.getLastResolvedAt())
                .fromCache(fromCache)
                .build();
    }
}
