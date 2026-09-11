package com.fraudService.flink.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemporalEvent {

    private String eventId;
    private String eventType; // TRANSACTION, BENEFICIARY_ADDED, LOGIN, DEVICE_CHANGE, PASSWORD_RESET
    private Long customerId;
    private BigDecimal amount;
    private String deviceId;
    private String ipAddress;
    private String beneficiaryId;
    private LocalDateTime timestamp;
    private long eventTimeEpochMs;

    public long getEventTimeEpochMs() {
        if (eventTimeEpochMs > 0) {
            return eventTimeEpochMs;
        }
        if (timestamp != null) {
            return timestamp.toInstant(ZoneOffset.UTC).toEpochMilli();
        }
        return Instant.now().toEpochMilli();
    }
}
