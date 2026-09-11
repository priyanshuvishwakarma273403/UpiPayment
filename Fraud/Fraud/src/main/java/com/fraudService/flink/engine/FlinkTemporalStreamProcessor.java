package com.fraudService.flink.engine;

import com.fraudService.flink.model.TemporalEvent;
import com.fraudService.flink.model.TemporalRiskSignal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Core Flink Temporal Stream Processor & CEP Engine.
 * Evaluates real-time event streams using Event-Time windowing, watermarks, deduplication, and late event handling.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlinkTemporalStreamProcessor {

    private final EventTimeWatermarkGenerator watermarkGenerator;
    private final EventDeduplicator deduplicator;

    // Per-customer event state buffers: customerId -> List<TemporalEvent>
    private final Map<Long, List<TemporalEvent>> eventBuffers = new HashMap<>();

    // Side-output storage for late events
    @Getter
    private final List<TemporalEvent> lateEventsSideOutput = new ArrayList<>();

    // Generated signals list (for auditing and testing)
    @Getter
    private final List<TemporalRiskSignal> generatedSignals = new ArrayList<>();

    private static final long VELOCITY_WINDOW_MS = 30_000L; // 30 seconds
    private static final long BENEFICIARY_WINDOW_MS = 120_000L; // 2 minutes
    private static final long ATO_SEQUENCE_WINDOW_MS = 300_000L; // 5 minutes
    private static final BigDecimal ATO_LARGE_TX_THRESHOLD = new BigDecimal("50000");

    /**
     * Processes an incoming event in the stream.
     * @return List of newly triggered TemporalRiskSignal items (if any).
     */
    public synchronized List<TemporalRiskSignal> processEvent(TemporalEvent event) {
        if (event == null || event.getCustomerId() == null) {
            return Collections.emptyList();
        }

        // 1. Deduplication check
        if (deduplicator.isDuplicate(event.getEventId())) {
            log.info("Ignored duplicate eventId={} in Flink stream processor", event.getEventId());
            return Collections.emptyList();
        }

        long eventTime = event.getEventTimeEpochMs();

        // 2. Late event evaluation against Watermark
        if (watermarkGenerator.isLate(eventTime)) {
            log.warn("Late event detected past watermark | eventId={} | eventTime={} | currentWatermark={}",
                    event.getEventId(), eventTime, watermarkGenerator.getCurrentWatermark());
            lateEventsSideOutput.add(event);
            return Collections.emptyList();
        }

        // Advance watermark
        watermarkGenerator.observeTimestamp(eventTime);

        // 3. Buffer event in customer state
        Long customerId = event.getCustomerId();
        List<TemporalEvent> buffer = eventBuffers.computeIfAbsent(customerId, k -> new ArrayList<>());
        buffer.add(event);

        // Sort events in buffer by event-time to handle out-of-order arrival
        buffer.sort(Comparator.comparingLong(TemporalEvent::getEventTimeEpochMs));

        // 4. Evaluate temporal CEP patterns
        List<TemporalRiskSignal> newSignals = new ArrayList<>();

        evalVelocitySpike(customerId, buffer, newSignals);
        evalRapidBeneficiary(customerId, buffer, newSignals);
        evalAtoSequence(customerId, buffer, newSignals);

        generatedSignals.addAll(newSignals);
        return newSignals;
    }

    /**
     * Pattern 1: 5+ transactions within 30 seconds sliding window.
     */
    private void evalVelocitySpike(Long customerId, List<TemporalEvent> buffer, List<TemporalRiskSignal> newSignals) {
        if (buffer.isEmpty()) return;

        long latestTime = buffer.get(buffer.size() - 1).getEventTimeEpochMs();
        long windowStart = latestTime - VELOCITY_WINDOW_MS;

        List<TemporalEvent> txInWindow = new ArrayList<>();
        for (TemporalEvent e : buffer) {
            if ("TRANSACTION".equalsIgnoreCase(e.getEventType())
                    && e.getEventTimeEpochMs() >= windowStart
                    && e.getEventTimeEpochMs() <= latestTime) {
                txInWindow.add(e);
            }
        }

        if (txInWindow.size() >= 5) {
            List<String> eventIds = txInWindow.stream().map(TemporalEvent::getEventId).toList();
            TemporalRiskSignal signal = TemporalRiskSignal.builder()
                    .signalId("SIG-VEL-" + UUID.randomUUID().toString().substring(0, 8))
                    .customerId(customerId)
                    .patternType("VELOCITY_SPIKE_30S")
                    .severity("HIGH_RISK")
                    .riskScoreAddition(25)
                    .windowStart(windowStart)
                    .windowEnd(latestTime)
                    .contributingEventIds(eventIds)
                    .eventSequenceDescription(String.format("%d transactions detected within 30s event window for customerId=%d", txInWindow.size(), customerId))
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("Flink CEP Triggered: {} for customerId={}", signal.getPatternType(), customerId);
            newSignals.add(signal);
        }
    }

    /**
     * Pattern 2: 3+ beneficiary creations within 2 minutes sliding window.
     */
    private void evalRapidBeneficiary(Long customerId, List<TemporalEvent> buffer, List<TemporalRiskSignal> newSignals) {
        if (buffer.isEmpty()) return;

        long latestTime = buffer.get(buffer.size() - 1).getEventTimeEpochMs();
        long windowStart = latestTime - BENEFICIARY_WINDOW_MS;

        List<TemporalEvent> benInWindow = new ArrayList<>();
        for (TemporalEvent e : buffer) {
            if ("BENEFICIARY_ADDED".equalsIgnoreCase(e.getEventType())
                    && e.getEventTimeEpochMs() >= windowStart
                    && e.getEventTimeEpochMs() <= latestTime) {
                benInWindow.add(e);
            }
        }

        if (benInWindow.size() >= 3) {
            List<String> eventIds = benInWindow.stream().map(TemporalEvent::getEventId).toList();
            TemporalRiskSignal signal = TemporalRiskSignal.builder()
                    .signalId("SIG-BEN-" + UUID.randomUUID().toString().substring(0, 8))
                    .customerId(customerId)
                    .patternType("RAPID_BENEFICIARY_2M")
                    .severity("SUSPICIOUS")
                    .riskScoreAddition(30)
                    .windowStart(windowStart)
                    .windowEnd(latestTime)
                    .contributingEventIds(eventIds)
                    .eventSequenceDescription(String.format("%d beneficiaries added within 2m event window for customerId=%d", benInWindow.size(), customerId))
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("Flink CEP Triggered: {} for customerId={}", signal.getPatternType(), customerId);
            newSignals.add(signal);
        }
    }

    /**
     * Pattern 3: ATO Sequence (LOGIN -> DEVICE_CHANGE -> BENEFICIARY_ADDED -> LARGE_TRANSACTION) within 5 minutes.
     */
    private void evalAtoSequence(Long customerId, List<TemporalEvent> buffer, List<TemporalRiskSignal> newSignals) {
        if (buffer.isEmpty()) return;

        long latestTime = buffer.get(buffer.size() - 1).getEventTimeEpochMs();
        long windowStart = latestTime - ATO_SEQUENCE_WINDOW_MS;

        // Filter events in current 5-minute window
        List<TemporalEvent> windowEvents = buffer.stream()
                .filter(e -> e.getEventTimeEpochMs() >= windowStart && e.getEventTimeEpochMs() <= latestTime)
                .sorted(Comparator.comparingLong(TemporalEvent::getEventTimeEpochMs))
                .toList();

        // Seek sequential match: LOGIN -> DEVICE_CHANGE -> BENEFICIARY_ADDED -> LARGE_TRANSACTION
        TemporalEvent loginEvent = null;
        TemporalEvent deviceChangeEvent = null;
        TemporalEvent benEvent = null;
        TemporalEvent largeTxEvent = null;

        for (TemporalEvent e : windowEvents) {
            String type = e.getEventType().toUpperCase();

            if (loginEvent == null && "LOGIN".equals(type)) {
                loginEvent = e;
            } else if (loginEvent != null && deviceChangeEvent == null && "DEVICE_CHANGE".equals(type)) {
                deviceChangeEvent = e;
            } else if (deviceChangeEvent != null && benEvent == null && "BENEFICIARY_ADDED".equals(type)) {
                benEvent = e;
            } else if (benEvent != null && largeTxEvent == null && "TRANSACTION".equals(type)) {
                if (e.getAmount() != null && e.getAmount().compareTo(ATO_LARGE_TX_THRESHOLD) >= 0) {
                    largeTxEvent = e;
                }
            }
        }

        if (loginEvent != null && deviceChangeEvent != null && benEvent != null && largeTxEvent != null) {
            List<String> eventIds = List.of(
                    loginEvent.getEventId(),
                    deviceChangeEvent.getEventId(),
                    benEvent.getEventId(),
                    largeTxEvent.getEventId()
            );

            TemporalRiskSignal signal = TemporalRiskSignal.builder()
                    .signalId("SIG-ATO-" + UUID.randomUUID().toString().substring(0, 8))
                    .customerId(customerId)
                    .patternType("ATO_SEQUENCE_5M")
                    .severity("REQUIRES_INVESTIGATION")
                    .riskScoreAddition(35)
                    .windowStart(loginEvent.getEventTimeEpochMs())
                    .windowEnd(largeTxEvent.getEventTimeEpochMs())
                    .contributingEventIds(eventIds)
                    .eventSequenceDescription(String.format("ATO Sequence detected: LOGIN -> DEVICE_CHANGE -> BENEFICIARY_ADDED -> LARGE_TX (₹%s) within 5m for customerId=%d",
                            largeTxEvent.getAmount(), customerId))
                    .timestamp(LocalDateTime.now())
                    .build();

            log.info("Flink CEP Triggered: {} for customerId={}", signal.getPatternType(), customerId);
            newSignals.add(signal);
        }
    }

    public synchronized void resetState() {
        eventBuffers.clear();
        lateEventsSideOutput.clear();
        generatedSignals.clear();
        watermarkGenerator.reset();
        deduplicator.clear();
    }
}
