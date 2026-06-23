package com.upimesh.risk.service;

import com.upimesh.risk.model.dto.DeviceResult;
import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.repository.RiskProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceFingerprintService {

    private final RiskProfileRepository profileRepository;

    @Value("${risk.new-device-boost:0.2}")
    private double newDeviceBoost;

    public DeviceResult analyzeDevice(String deviceId, String userId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return new DeviceResult(true, newDeviceBoost);
        }

        String hashedDevice = hashDeviceId(deviceId);

        Optional<RiskProfile> profileOpt = profileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            // New user, this device is naturally new
            return new DeviceResult(true, newDeviceBoost);
        }

        Set<String> knownDevices = profileOpt.get().getKnownDevices();
        if (knownDevices == null || knownDevices.isEmpty()) {
            return new DeviceResult(true, newDeviceBoost);
        }

        if (knownDevices.contains(hashedDevice)) {
            log.debug("Known device match found for user: {}", userId);
            return new DeviceResult(false, 0.0);
        }

        log.info("New device detected for user: {}, assigning boost: {}", userId, newDeviceBoost);
        return new DeviceResult(true, newDeviceBoost);
    }

    public String hashDeviceId(String deviceId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(deviceId.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error hashing device ID, returning original: {}", e.getMessage());
            return deviceId;
        }
    }
}
