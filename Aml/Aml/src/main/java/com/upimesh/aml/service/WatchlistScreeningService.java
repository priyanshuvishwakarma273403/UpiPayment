package com.upimesh.aml.service;

import com.upimesh.aml.model.dto.WatchlistResult;
import com.upimesh.aml.model.entity.WatchlistEntry;
import com.upimesh.aml.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchlistScreeningService {

    private final WatchlistRepository watchlistRepository;

    public WatchlistResult screenName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new WatchlistResult(false, null, null, null);
        }

        String cleanedName = name.trim().toLowerCase();
        log.debug("Screening name: {}", cleanedName);

        List<WatchlistEntry> activeEntries = watchlistRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(cleanedName);
        if (activeEntries.isEmpty()) {
            activeEntries = watchlistRepository.findAll().stream()
                    .filter(WatchlistEntry::isActive)
                    .toList();
        }

        for (WatchlistEntry entry : activeEntries) {
            double similarity = calculateSimilarity(cleanedName, entry.getName().toLowerCase());
            log.trace("Comparing '{}' with '{}', similarity: {}", cleanedName, entry.getName(), similarity);
            if (similarity >= 0.80) {
                log.info("Watchlist match found! '{}' matches '{}' with similarity: {}", name, entry.getName(), similarity);
                return new WatchlistResult(true, entry.getName(), entry.getEntityType(), entry.getSource());
            }
        }

        return new WatchlistResult(false, null, null, null);
    }

    private double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) {
            return 1.0;
        }
        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 == 0 || len2 == 0) {
            return 0.0;
        }

        int[][] dp = new int[len1 + 1][len2 + 1];
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        int distance = dp[len1][len2];
        int maxLength = Math.max(len1, len2);
        return 1.0 - ((double) distance / maxLength);
    }
}
