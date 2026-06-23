package com.upimesh.risk.service;

import com.upimesh.risk.model.dto.LocationResult;
import com.upimesh.risk.model.entity.RiskProfile;
import com.upimesh.risk.repository.RiskProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationAnalysisService {

    private final RiskProfileRepository profileRepository;

    public LocationResult analyzeLocation(String city, String userId) {
        if (city == null || city.trim().isEmpty()) {
            return new LocationResult(false, 0.0, "Location data not available");
        }

        Optional<RiskProfile> profileOpt = profileRepository.findByUserId(userId);
        if (profileOpt.isEmpty()) {
            return new LocationResult(false, 0.0, "First city registered for new user");
        }

        Set<String> usualCities = profileOpt.get().getUsualCities();
        if (usualCities == null || usualCities.isEmpty()) {
            return new LocationResult(false, 0.0, "First city registered for user profile");
        }

        String normalizedCity = city.trim().toLowerCase();
        boolean matchFound = usualCities.stream()
                .anyMatch(c -> c.trim().toLowerCase().equals(normalizedCity));

        if (matchFound) {
            return new LocationResult(false, 0.0, "Location matches user profile history");
        }

        String reason = String.format("Transaction executed from anomalous location: %s (not in usual cities: %s)", city, usualCities);
        log.warn("Location anomaly for user: {}. Reason: {}", userId, reason);
        return new LocationResult(true, 0.25, reason);
    }
}
