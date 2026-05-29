package com.syncService.Sync.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syncService.Sync.entity.SyncRecord;
import com.syncService.Sync.repository.SyncRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncService {

    private final SyncRecordRepository syncRecordRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_PAYMENT_INITIATED = "payment_initiated";
    private static final String TOPIC_SYNC_COMPLETED    = "sync_completed";
    private static final int    MAX_RETRY_ATTEMPTS      = 3;

    @Transactional
    public Map<String, Object> syncPendingPayments(Long userId) {
        log.info("Manual sync triggered for userId={}", userId);
        List<SyncRecord> pending = syncRecordRepository
                .findBySenderIdAndStatusOrderByCreatedAtAsc(userId, SyncRecord.SyncStatus.PENDING);
        return processSyncRecords(pending, "manual");
    }

    @Transactional
    public void syncAllPendingPayments() {
        log.info("Scheduled sync: fetching all PENDING payments");
        List<SyncRecord> pending = syncRecordRepository
                .findByStatusAndRetryCountLessThanOrderByCreatedAtAsc(
                        SyncRecord.SyncStatus.PENDING, MAX_RETRY_ATTEMPTS);
        if (pending.isEmpty()) { log.info("No pending payments"); return; }
        processSyncRecords(pending, "scheduled");
    }

    @Transactional
    public void retryFailedSyncs() {
        log.info("Retrying FAILED sync records...");
        List<SyncRecord> failed = syncRecordRepository
                .findByStatusAndRetryCountLessThan(SyncRecord.SyncStatus.FAILED, MAX_RETRY_ATTEMPTS);
        failed.forEach(r -> {
            r.setStatus(SyncRecord.SyncStatus.PENDING);
            r.setNextRetryAt(LocalDateTime.now());
            syncRecordRepository.save(r);
        });
        if (!failed.isEmpty()) syncAllPendingPayments();
    }

    @Transactional
    public SyncRecord registerOfflinePayment(String paymentId, Long senderId, String paymentJson) {
        if (syncRecordRepository.existsByPaymentId(paymentId)) {
            return syncRecordRepository.findByPaymentId(paymentId).orElseThrow();
        }
        SyncRecord r = SyncRecord.builder()
                .paymentId(paymentId).senderId(senderId).paymentJson(paymentJson)
                .status(SyncRecord.SyncStatus.PENDING).retryCount(0)
                .nextRetryAt(LocalDateTime.now()).build();
        return syncRecordRepository.save(r);
    }

    private Map<String, Object> processSyncRecords(List<SyncRecord> records, String ctx) {
        int processed = 0, failed = 0;
        List<String> processedIds = new ArrayList<>(), failedIds = new ArrayList<>();

        for (SyncRecord record : records) {
            String paymentId = record.getPaymentId();
            try {
                if (record.getStatus() == SyncRecord.SyncStatus.SYNCED) continue;
                validatePaymentJson(record.getPaymentJson());

                kafkaTemplate.send(TOPIC_PAYMENT_INITIATED, paymentId, record.getPaymentJson());
                kafkaTemplate.send(TOPIC_SYNC_COMPLETED,    paymentId, record.getPaymentJson());

                record.setStatus(SyncRecord.SyncStatus.SYNCED);
                record.setSyncedAt(LocalDateTime.now());
                syncRecordRepository.save(record);
                processedIds.add(paymentId);
                processed++;
                log.info("[{}] Synced: {}", ctx, paymentId);

            } catch (Exception e) {
                log.error("[{}] Failed {}: {}", ctx, paymentId, e.getMessage());
                record.setRetryCount(record.getRetryCount() + 1);
                record.setLastFailureReason(e.getMessage());
                if (record.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
                    record.setStatus(SyncRecord.SyncStatus.PERMANENTLY_FAILED);
                } else {
                    long backoff = (long) Math.pow(2, record.getRetryCount()) * 5;
                    record.setStatus(SyncRecord.SyncStatus.FAILED);
                    record.setNextRetryAt(LocalDateTime.now().plusMinutes(backoff));
                }
                syncRecordRepository.save(record);
                failedIds.add(paymentId);
                failed++;
            }
        }
        return Map.of("total", records.size(), "processed", processed, "failed", failed,
                "processedIds", processedIds, "failedIds", failedIds,
                "completedAt", LocalDateTime.now().toString());
    }

    private void validatePaymentJson(String json) throws JsonProcessingException {
        if (json == null || json.isBlank()) throw new IllegalArgumentException("Empty payment JSON");
        Map<?, ?> map = objectMapper.readValue(json, Map.class);
        List<String> missing = List.of("paymentId", "senderId", "amount").stream()
                .filter(f -> !map.containsKey(f)).collect(Collectors.toList());
        if (!missing.isEmpty()) throw new IllegalArgumentException("Missing fields: " + missing);
    }
}
