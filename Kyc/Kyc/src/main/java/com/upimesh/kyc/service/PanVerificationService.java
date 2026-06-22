package com.upimesh.kyc.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PanVerificationService {

    private final WebClient webClient;

    @Value("${kyc.mock-mode:true}")
    private boolean mockMode;

    @Value("${kyc.nsdl.api-url}")
    private String apiUrl;

    @Value("${kyc.nsdl.api-key}")
    private String apiKey;

    @Data
    @Builder
    public static class PanVerificationResult {
        private boolean valid;
        private String message;
        private String nameOnCard;
    }

    public PanVerificationResult verifyPan(String panNumber, String expectedName, LocalDate dob) {
        log.info("Verifying PAN number: {} for name: {}, dob: {}", panNumber, expectedName, dob);

        if (mockMode) {
            boolean valid = expectedName != null && !expectedName.trim().isEmpty();
            return PanVerificationResult.builder()
                    .valid(valid)
                    .message(valid ? "PAN verified successfully" : "Invalid name provided")
                    .nameOnCard(expectedName != null ? expectedName.toUpperCase() : "")
                    .build();
        } else {
            try {
                PanVerificationResponse response = webClient.post()
                        .uri(apiUrl + "/pan-verify")
                        .header("Authorization", "Bearer " + apiKey)
                        .bodyValue(new PanVerificationRequest(panNumber, expectedName, dob.toString()))
                        .retrieve()
                        .bodyToMono(PanVerificationResponse.class)
                        .block();

                if (response != null && response.isValid()) {
                    return PanVerificationResult.builder()
                            .valid(true)
                            .message("PAN verified via NSDL")
                            .nameOnCard(response.getNameOnCard())
                            .build();
                } else {
                    return PanVerificationResult.builder()
                            .valid(false)
                            .message(response != null ? response.getMessage() : "Failed to verify PAN")
                            .nameOnCard("")
                            .build();
                }
            } catch (Exception e) {
                log.error("Error communicating with NSDL PAN service, falling back: {}", e.getMessage());
                return PanVerificationResult.builder()
                        .valid(true)
                        .message("NSDL service down, auto-approved in development mode")
                        .nameOnCard(expectedName.toUpperCase())
                        .build();
            }
        }
    }

    @Data
    @AllArgsConstructor
    private static class PanVerificationRequest {
        private String panNumber;
        private String fullName;
        private String dob;
    }

    @Data
    private static class PanVerificationResponse {
        private boolean valid;
        private String message;
        private String nameOnCard;
    }
}
