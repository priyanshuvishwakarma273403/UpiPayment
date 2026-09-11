package com.fraudService.flink.engine;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Bounded out-of-orderness Event Time Watermark Generator following Apache Flink semantics.
 * Watermark = MaxEventTimeSeen - outOfOrdernessMillis (Default: 5000ms).
 */
@Component
@Slf4j
public class EventTimeWatermarkGenerator {

    @Getter
    private final long outOfOrdernessMillis;
    private final AtomicLong maxTimestampSeen = new AtomicLong(Long.MIN_VALUE);

    public EventTimeWatermarkGenerator() {
        this(5000L); // 5 seconds bounded out-of-orderness tolerance
    }

    public EventTimeWatermarkGenerator(long outOfOrdernessMillis) {
        this.outOfOrdernessMillis = outOfOrdernessMillis;
    }

    /** Updates max timestamp seen and returns current calculated watermark. */
    public long observeTimestamp(long timestamp) {
        maxTimestampSeen.accumulateAndGet(timestamp, Math::max);
        return getCurrentWatermark();
    }

    /** Returns current watermark epoch ms. */
    public long getCurrentWatermark() {
        long maxSeen = maxTimestampSeen.get();
        if (maxSeen == Long.MIN_VALUE) {
            return Long.MIN_VALUE;
        }
        return maxSeen - outOfOrdernessMillis;
    }

    /** Evaluates if an event timestamp is late relative to the current watermark. */
    public boolean isLate(long eventTimestamp) {
        long currentWatermark = getCurrentWatermark();
        if (currentWatermark == Long.MIN_VALUE) {
            return false;
        }
        return eventTimestamp < currentWatermark;
    }

    /** Resets generator state for testing purposes. */
    public void reset() {
        maxTimestampSeen.set(Long.MIN_VALUE);
    }
}
