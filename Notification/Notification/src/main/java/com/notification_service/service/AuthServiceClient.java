package com.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;
    private static final String AUTH_SERVICE_URL = "http://auth-service/auth";

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserDetails(Long userId) {
        try {
            return restTemplate.getForObject(AUTH_SERVICE_URL + "/users/" + userId, Map.class);
        } catch (Exception e) {
            log.error("Failed to fetch user details for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUserDetailsByUpiId(String upiId) {
        try {
            return restTemplate.getForObject(AUTH_SERVICE_URL + "/users/upi/" + upiId, Map.class);
        } catch (Exception e) {
            log.error("Failed to fetch user details for upiId={}: {}", upiId, e.getMessage());
            return null;
        }
    }
}
