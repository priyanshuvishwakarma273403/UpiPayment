package com.npci.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * NpciClient — Handles all HTTP communication with NPCI.
 *
 * In production: uses real NPCI APIs with mutual TLS
 * In dev/test:   uses mock server (use-mock: true in config)
 *
 * Circuit breaker wraps all calls — if NPCI is down,
 * we fail fast instead of hanging the user's request.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class NpciClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PayloadSigningService signingService;

    @Value("${npci.base-url}")
    private String npciBaseUrl;

    @Value("${npci.mock-url}")
    private String mockUrl;

    @Value("${npci.use-mock}")
    private boolean useMock;

    @Value("${npci.api-key}")
    private String apiKey;

    @Value("${npci.merchant-id}")
    private String merchantId;

    // ─── Transaction APIs ─────────────────────────────────────────────────────

    /**
     * Send payment request to NPCI.
     * Returns NPCI's raw JSON response node.
     */

}
