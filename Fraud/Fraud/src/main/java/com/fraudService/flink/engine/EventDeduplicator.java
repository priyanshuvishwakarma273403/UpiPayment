package com.fraudService.flink.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deduplicates events based on unique eventId within a sliding TTL retention window.
 */
@Component
@Slf4j
public class EventDeduplicator {

    private final Map<String, Long> seenEvents = new ConcurrentHashMap<>();
    private final long ttlMillis;

    public EventDeduplicator() {
        this(600_000L); // 10 minutes default TTL retention
    }

    public EventDeduplicator(long ttlMillis) {
        this.ttlMillis = ttlMillis;
    }

    /** Returns true if event has already been seen (duplicate), otherwise records it and returns false. */
    public boolean isDuplicate(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            return false;
        }

        long now = System.currentTimeMillis();
        cleanExpiredEntries(now);

        Long existingTime = seenEvents.putIfAbsent(eventId, now);
        if (existingTime != null) {
            log.warn("Duplicate event detected in Flink stream | eventId={}", eventId);
            return true;
        }
        return false;
    }

    private void cleanExpiredEntries(long now) {
        if (seenEvents.size() > 5000) {
            seenEvents.entrySet().removeIf(entry -> (now - entry.getValue()) > ttlMillis);
        }
    }

    public void clear() {
        seenEvents.clear();
    }
}
