package com.fraudService.flink;

import com.fraudService.flink.engine.EventDeduplicator;
import com.fraudService.flink.engine.EventTimeWatermarkGenerator;
import com.fraudService.flink.engine.FlinkTemporalStreamProcessor;
import com.fraudService.flink.model.TemporalEvent;
import com.fraudService.flink.model.TemporalRiskSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlinkTemporalStreamProcessorTest {

    private FlinkTemporalStreamProcessor processor;
    private EventTimeWatermarkGenerator watermarkGenerator;
    private EventDeduplicator deduplicator;

    @BeforeEach
    void setUp() {
        watermarkGenerator = new EventTimeWatermarkGenerator(5000L); // 5s watermark tolerance
        deduplicator = new EventDeduplicator(600_000L);
        processor = new FlinkTemporalStreamProcessor(watermarkGenerator, deduplicator);
        processor.resetState();
    }

    @Test
    @DisplayName("Pattern 1: 5 transactions within 30s triggers VELOCITY_SPIKE_30S signal")
    void testTransactionVelocitySpike30s() {
        Long customerId = 101L;
        long baseTime = 1700000000000L;

        for (int i = 1; i <= 5; i++) {
            TemporalEvent event = TemporalEvent.builder()
                    .eventId("EVT-TX-" + i)
                    .eventType("TRANSACTION")
                    .customerId(customerId)
                    .amount(new BigDecimal("1000"))
                    .eventTimeEpochMs(baseTime + (i * 4000L)) // 4s intervals = 20s total window
                    .timestamp(LocalDateTime.now())
                    .build();
            processor.processEvent(event);
        }

        List<TemporalRiskSignal> signals = processor.getGeneratedSignals();
        assertFalse(signals.isEmpty(), "Should trigger at least one temporal risk signal");

        TemporalRiskSignal velocitySignal = signals.stream()
                .filter(s -> "VELOCITY_SPIKE_30S".equals(s.getPatternType()))
                .findFirst()
                .orElse(null);

        assertNotNull(velocitySignal, "VELOCITY_SPIKE_30S signal must be present");
        assertEquals(customerId, velocitySignal.getCustomerId());
        assertEquals("HIGH_RISK", velocitySignal.getSeverity());
        assertEquals(25, velocitySignal.getRiskScoreAddition());
    }

    @Test
    @DisplayName("Pattern 2: 3 beneficiaries within 2 minutes triggers RAPID_BENEFICIARY_2M signal")
    void testRapidBeneficiary2m() {
        Long customerId = 102L;
        long baseTime = 1700000000000L;

        for (int i = 1; i <= 3; i++) {
            TemporalEvent event = TemporalEvent.builder()
                    .eventId("EVT-BEN-" + i)
                    .eventType("BENEFICIARY_ADDED")
                    .customerId(customerId)
                    .beneficiaryId("BEN-" + i)
                    .eventTimeEpochMs(baseTime + (i * 30_000L)) // 30s intervals = 90s total window
                    .timestamp(LocalDateTime.now())
                    .build();
            processor.processEvent(event);
        }

        List<TemporalRiskSignal> signals = processor.getGeneratedSignals();
        TemporalRiskSignal benSignal = signals.stream()
                .filter(s -> "RAPID_BENEFICIARY_2M".equals(s.getPatternType()))
                .findFirst()
                .orElse(null);

        assertNotNull(benSignal, "RAPID_BENEFICIARY_2M signal must be present");
        assertEquals(customerId, benSignal.getCustomerId());
        assertEquals("SUSPICIOUS", benSignal.getSeverity());
        assertEquals(30, benSignal.getRiskScoreAddition());
    }

    @Test
    @DisplayName("Pattern 3: ATO sequence (LOGIN -> DEVICE_CHANGE -> BENEFICIARY_ADDED -> LARGE_TX) triggers ATO_SEQUENCE_5M")
    void testAtoSequence5m() {
        Long customerId = 103L;
        long baseTime = 1700000000000L;

        // 1. LOGIN
        processor.processEvent(TemporalEvent.builder()
                .eventId("EVT-LOGIN-1")
                .eventType("LOGIN")
                .customerId(customerId)
                .eventTimeEpochMs(baseTime)
                .timestamp(LocalDateTime.now())
                .build());

        // 2. DEVICE_CHANGE
        processor.processEvent(TemporalEvent.builder()
                .eventId("EVT-DEV-1")
                .eventType("DEVICE_CHANGE")
                .customerId(customerId)
                .deviceId("DEV-NEW-99")
                .eventTimeEpochMs(baseTime + 30_000L) // +30s
                .timestamp(LocalDateTime.now())
                .build());

        // 3. BENEFICIARY_ADDED
        processor.processEvent(TemporalEvent.builder()
                .eventId("EVT-BEN-99")
                .eventType("BENEFICIARY_ADDED")
                .customerId(customerId)
                .beneficiaryId("BEN-SUSPICIOUS-1")
                .eventTimeEpochMs(baseTime + 60_000L) // +60s
                .timestamp(LocalDateTime.now())
                .build());

        // 4. LARGE_TRANSACTION (₹75,000)
        processor.processEvent(TemporalEvent.builder()
                .eventId("EVT-TX-LARGE-1")
                .eventType("TRANSACTION")
                .customerId(customerId)
                .amount(new BigDecimal("75000"))
                .eventTimeEpochMs(baseTime + 120_000L) // +120s
                .timestamp(LocalDateTime.now())
                .build());

        List<TemporalRiskSignal> signals = processor.getGeneratedSignals();
        TemporalRiskSignal atoSignal = signals.stream()
                .filter(s -> "ATO_SEQUENCE_5M".equals(s.getPatternType()))
                .findFirst()
                .orElse(null);

        assertNotNull(atoSignal, "ATO_SEQUENCE_5M signal must be present");
        assertEquals(customerId, atoSignal.getCustomerId());
        assertEquals("REQUIRES_INVESTIGATION", atoSignal.getSeverity());
        assertEquals(35, atoSignal.getRiskScoreAddition());
        assertEquals(4, atoSignal.getContributingEventIds().size());
    }

    @Test
    @DisplayName("Deduplication: Duplicate event IDs are ignored and do not double count")
    void testDuplicateEventsHandling() {
        Long customerId = 104L;
        long baseTime = 1700000000000L;

        TemporalEvent event1 = TemporalEvent.builder()
                .eventId("EVT-DUP-1")
                .eventType("BENEFICIARY_ADDED")
                .customerId(customerId)
                .eventTimeEpochMs(baseTime)
                .timestamp(LocalDateTime.now())
                .build();

        // Process event twice
        processor.processEvent(event1);
        processor.processEvent(event1);

        // Submit second distinct event
        TemporalEvent event2 = TemporalEvent.builder()
                .eventId("EVT-DUP-2")
                .eventType("BENEFICIARY_ADDED")
                .customerId(customerId)
                .eventTimeEpochMs(baseTime + 10_000L)
                .timestamp(LocalDateTime.now())
                .build();
        processor.processEvent(event2);

        // Only 2 unique events submitted, so RAPID_BENEFICIARY_2M (requires 3) should NOT trigger
        boolean triggered = processor.getGeneratedSignals().stream()
                .anyMatch(s -> "RAPID_BENEFICIARY_2M".equals(s.getPatternType()));

        assertFalse(triggered, "Duplicate event should be ignored and not cause trigger");
    }

    @Test
    @DisplayName("Out-of-order events within watermark tolerance are sorted and processed in event-time")
    void testOutOfOrderEventsHandling() {
        Long customerId = 105L;
        long baseTime = 1700000000000L;

        // Submit out-of-order events within 5s watermark tolerance
        TemporalEvent event2 = TemporalEvent.builder()
                .eventId("EVT-OOO-2")
                .eventType("DEVICE_CHANGE")
                .customerId(customerId)
                .eventTimeEpochMs(baseTime + 10_000L)
                .build();

        TemporalEvent event1 = TemporalEvent.builder()
                .eventId("EVT-OOO-1")
                .eventType("LOGIN")
                .customerId(customerId)
                .eventTimeEpochMs(baseTime + 8_000L) // Arrives after event2, but eventTime is earlier
                .build();

        processor.processEvent(event2);
        processor.processEvent(event1);

        assertEquals(0, processor.getLateEventsSideOutput().size(), "Out-of-order events within watermark should not be marked late");
    }

    @Test
    @DisplayName("Late events past watermark allowed lateness are routed to side output")
    void testLateEventsHandling() {
        Long customerId = 106L;
        long baseTime = 1700000000000L;

        // 1. Event at baseTime + 100s advances watermark to baseTime + 95s
        TemporalEvent futureEvent = TemporalEvent.builder()
                .eventId("EVT-FUTURE")
                .eventType("LOGIN")
                .customerId(customerId)
                .eventTimeEpochMs(baseTime + 100_000L)
                .build();
        processor.processEvent(futureEvent);

        // 2. Late event at baseTime + 10s (85s behind watermark)
        TemporalEvent lateEvent = TemporalEvent.builder()
                .eventId("EVT-LATE")
                .eventType("TRANSACTION")
                .customerId(customerId)
                .eventTimeEpochMs(baseTime + 10_000L)
                .build();
        processor.processEvent(lateEvent);

        List<TemporalEvent> lateSideOutput = processor.getLateEventsSideOutput();
        assertEquals(1, lateSideOutput.size(), "Late event past watermark should be routed to side output");
        assertEquals("EVT-LATE", lateSideOutput.get(0).getEventId());
    }
}
