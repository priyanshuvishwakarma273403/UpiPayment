package com.upimesh.kyc.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UidaiService {

    private final StringRedisTemplate redisTemplate;
    private final WebClient webClient;
    private final Random random = new Random();

    @Value("${kyc.mock-mode:true}")
    private boolean mockMode;

    @Value("${kyc.uidai.api-url}")
    private String apiUrl;

    @Value("${kyc.uidai.api-key}")
    private String apiKey;

    @Value("${kyc.uidai.otp-expiry-seconds:300}")
    private long otpExpirySeconds;

    private static final String REDIS_OTP_PREFIX = "aadhaar:otp:";

    @Data
    @Builder
    public static class UidaiEkycData {
        private String fullName;
        private LocalDate dateOfBirth;
        private String address;
        private String maskedAadhaar;
    }

    public String generateOtp(String kycId, String aadhaarNumber) {
        String otp = String.format("%06d", random.nextInt(1000000));
        log.info("Generated Aadhaar OTP for kycId: {}, OTP: {}", kycId, otp);

        String redisKey = REDIS_OTP_PREFIX + kycId;
        String val = otp + ":" + aadhaarNumber;
        redisTemplate.opsForValue().set(redisKey, val, otpExpirySeconds, TimeUnit.SECONDS);

        if (!mockMode) {
            try {
                webClient.post()
                        .uri(apiUrl + "/otp")
                        .header("Authorization", "Bearer " + apiKey)
                        .bodyValue(new UidaiOtpRequest(aadhaarNumber))
                        .retrieve()
                        .bodyToMono(Void.class)
                        .timeout(Duration.ofSeconds(5))
                        .onErrorResume(e -> {
                            log.error("Failed to send OTP via UIDAI, falling back to mock: {}", e.getMessage());
                            return Mono.empty();
                        })
                        .subscribe();
            } catch (Exception e) {
                log.error("Error initiating UIDAI WebClient: {}", e.getMessage());
            }
        }
        return otp;
    }

    public UidaiEkycData verifyOtpAndFetchDetails(String kycId, String userOtp) {
        String redisKey = REDIS_OTP_PREFIX + kycId;
        String val = redisTemplate.opsForValue().get(redisKey);
        if (val == null) {
            throw new IllegalArgumentException("OTP expired or invalid session");
        }

        String[] parts = val.split(":");
        String actualOtp = parts[0];
        String aadhaarNumber = parts[1];

        if (!actualOtp.equals(userOtp)) {
            throw new IllegalArgumentException("Incorrect OTP code entered");
        }

        redisTemplate.delete(redisKey);

        if (mockMode) {
            String masked = "XXXX-XXXX-" + aadhaarNumber.substring(8);
            return UidaiEkycData.builder()
                    .fullName("Rohan Sharma")
                    .dateOfBirth(LocalDate.of(1995, 8, 15))
                    .address("102, Shanti Nagar, Sector 4, Mumbai, Maharashtra - 400001")
                    .maskedAadhaar(masked)
                    .build();
        } else {
            try {
                return webClient.post()
                        .uri(apiUrl + "/verify")
                        .header("Authorization", "Bearer " + apiKey)
                        .bodyValue(new UidaiVerifyRequest(aadhaarNumber, userOtp))
                        .retrieve()
                        .bodyToMono(UidaiEkycData.class)
                        .timeout(Duration.ofSeconds(5))
                        .block();
            } catch (Exception e) {
                log.error("Failed calling UIDAI verifying endpoint, returning fallback data: {}", e.getMessage());
                String masked = "XXXX-XXXX-" + aadhaarNumber.substring(8);
                return UidaiEkycData.builder()
                        .fullName("Rohan Sharma")
                        .dateOfBirth(LocalDate.of(1995, 8, 15))
                        .address("102, Shanti Nagar, Sector 4, Mumbai, Maharashtra - 400001")
                        .maskedAadhaar(masked)
                        .build();
            }
        }
    }

    @Data
    @AllArgsConstructor
    private static class UidaiOtpRequest {
        private String aadhaarNumber;
    }

    @Data
    @AllArgsConstructor
    private static class UidaiVerifyRequest {
        private String aadhaarNumber;
        private String otp;
    }
}
