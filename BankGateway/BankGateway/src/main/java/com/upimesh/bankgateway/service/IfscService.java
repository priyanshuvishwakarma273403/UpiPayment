package com.upimesh.bankgateway.service;

import com.upimesh.bankgateway.client.BankClientRegistry;
import com.upimesh.bankgateway.exception.InvalidIfscException;
import com.upimesh.bankgateway.model.entity.IfscDetail;
import com.upimesh.bankgateway.model.enums.BankCode;
import com.upimesh.bankgateway.model.response.IfscResponse;
import com.upimesh.bankgateway.repository.IfscDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IfscService {

    private final IfscDetailRepository ifscRepo;
    private final RestTemplate restTemplate;

    @Cacheable(value = "ifscCache", key = "#ifscCode.toUpperCase()")
    public IfscResponse getIfscDetails(String ifscCode) {
        if (ifscCode == null || !ifscCode.matches("^[A-Z]{4}0[A-Z0-9]{6}$")) {
            throw new InvalidIfscException("Invalid IFSC code format: " + ifscCode);
        }

        String lookupCode = ifscCode.toUpperCase();
        log.info("Resolving IFSC code: {}", lookupCode);

        // 1. Check local database cache
        var optionalDetail = ifscRepo.findByIfscCode(lookupCode);
        if (optionalDetail.isPresent()) {
            log.info("IFSC found in local database: {}", lookupCode);
            return mapToResponse(optionalDetail.get(), true);
        }

        // 2. Fetch from Razorpay API
        String url = "https://ifsc.razorpay.com/" + lookupCode;
        log.info("IFSC not in DB. Querying Razorpay API: {}", url);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = restTemplate.getForObject(url, Map.class);
            if (responseMap == null) {
                throw new InvalidIfscException("IFSC code not found: " + lookupCode);
            }

            // Derive bank code
            BankCode bankCode = BankCode.fromIfsc(lookupCode);
            if (bankCode == null) {
                throw new InvalidIfscException("Unsupported or unrecognized bank prefix in IFSC: " + lookupCode);
            }

            // Parse response
            String bankName = (String) responseMap.get("BANK");
            String branchName = (String) responseMap.get("BRANCH");
            String city = (String) responseMap.get("CITY");
            String district = (String) responseMap.get("DISTRICT");
            String state = (String) responseMap.get("STATE");
            String address = (String) responseMap.get("ADDRESS");
            String contact = (String) responseMap.get("CONTACT");
            
            boolean impsEnabled = Boolean.TRUE.equals(responseMap.get("IMPS"));
            boolean neftEnabled = Boolean.TRUE.equals(responseMap.get("NEFT"));
            boolean rtgsEnabled = Boolean.TRUE.equals(responseMap.get("RTGS"));

            IfscDetail detail = IfscDetail.builder()
                    .ifscCode(lookupCode)
                    .bankCode(bankCode)
                    .bankName(bankName != null ? bankName : bankCode.getDisplayName())
                    .branchName(branchName != null ? branchName : "Unknown Branch")
                    .city(city != null ? city : "Unknown City")
                    .district(district)
                    .state(state != null ? state : "Unknown State")
                    .address(address)
                    .contact(contact)
                    .impsEnabled(impsEnabled)
                    .neftEnabled(neftEnabled)
                    .rtgsEnabled(rtgsEnabled)
                    .build();

            detail = ifscRepo.save(detail);
            log.info("IFSC details successfully cached in local DB: {}", lookupCode);

            // Per prompt instructions, we can return fromCache=true or fromCache=false.
            // Let's set fromCache=true for DB/Caffeine hits and fromCache=false when fetched from API.
            return mapToResponse(detail, false);

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("IFSC code not found in Razorpay: {}", lookupCode);
            throw new InvalidIfscException("IFSC code not found: " + lookupCode);
        } catch (Exception e) {
            log.error("Failed to fetch IFSC details for: {}", lookupCode, e);
            throw new InvalidIfscException("Failed to fetch details for IFSC code: " + lookupCode);
        }
    }

    private IfscResponse mapToResponse(IfscDetail detail, boolean fromCache) {
        return IfscResponse.builder()
                .ifscCode(detail.getIfscCode())
                .bankCode(detail.getBankCode())
                .bankName(detail.getBankName())
                .branchName(detail.getBranchName())
                .city(detail.getCity())
                .district(detail.getDistrict())
                .state(detail.getState())
                .address(detail.getAddress())
                .contact(detail.getContact())
                .impsEnabled(detail.isImpsEnabled())
                .neftEnabled(detail.isNeftEnabled())
                .rtgsEnabled(detail.isRtgsEnabled())
                .fromCache(fromCache)
                .build();
    }
}
