package com.upimesh.settlement.service;

import com.upimesh.settlement.exception.*;
import com.upimesh.settlement.feign.BankGatewayClient;
import com.upimesh.settlement.feign.BankGatewayClient.MerchantBankDetailsDto;
import com.upimesh.settlement.feign.TransactionServiceClient;
import com.upimesh.settlement.feign.TransactionServiceClient.UnsettledTransactionDto;
import com.upimesh.settlement.model.entity.MerchantSettlement;
import com.upimesh.settlement.model.entity.SettlementBatch;
import com.upimesh.settlement.model.entity.SettlementTransaction;
import com.upimesh.settlement.model.enums.SettlementMode;
import com.upimesh.settlement.model.enums.SettlementStatus;
import com.upimesh.settlement.model.response.MerchantSettlementResponse;
import com.upimesh.settlement.model.response.SettlementBatchResponse;
import com.upimesh.settlement.model.response.SettlementReportResponse;
import com.upimesh.settlement.repository.MerchantSettlementRepository;
import com.upimesh.settlement.repository.SettlementBatchRepository;
import com.upimesh.settlement.repository.SettlementTransactionRepository;
import com.upimesh.settlement.util.SettlementEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final SettlementBatchRepository batchRepo;
    private final MerchantSettlementRepository merchantSettlementRepo;
    private final SettlementTransactionRepository settlementTxnRepo;
    
    private final TransactionServiceClient transactionServiceClient;
    private final BankGatewayClient bankGatewayClient;
    
    private final SettlementCalculationService calculationService;
    private final BankTransferService bankTransferService;

    @Value("${internal.service-key}")
    private String serviceKey;

    @Value("${settlement.min-amount:1.00}")
    private BigDecimal minSettleAmount;

    @Value("${settlement.max-retry:3}")
    private int maxRetryCount;

    @Transactional
    public SettlementBatchResponse runDailySettlement() {
        LocalDate today = LocalDate.now();
        log.info("Starting EOD Daily Settlement for Date: {}", today);

        // 1. Idempotency Check — Check if batch for today already exists
        var existingBatch = batchRepo.findBySettlementDate(today);
        if (existingBatch.isPresent()) {
            log.warn("Settlement batch for today already exists: {}", existingBatch.get().getBatchId());
            return mapToBatchResponse(existingBatch.get());
        }

        // 2. Create new SettlementBatch
        String batchId = "BAT" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        SettlementBatch batch = SettlementBatch.builder()
                .batchId(batchId)
                .settlementDate(today)
                .startTime(LocalDateTime.now())
                .status(SettlementStatus.PROCESSING)
                .totalMerchants(0)
                .totalTransactions(0)
                .totalAmount(BigDecimal.ZERO)
                .processedAmount(BigDecimal.ZERO)
                .failedAmount(BigDecimal.ZERO)
                .build();
        batch = batchRepo.save(batch);

        // Date range: yesterday midnight to today midnight
        String fromDateStr = today.minusDays(1).atStartOfDay().toString();
        String toDateStr = today.atStartOfDay().toString();

        // 3. Fetch unsettled transactions
        List<UnsettledTransactionDto> unsettledTxns;
        try {
            log.info("Fetching unsettled transactions from: {} to: {}", fromDateStr, toDateStr);
            unsettledTxns = transactionServiceClient.getUnsettledTransactions(serviceKey, fromDateStr, toDateStr);
        } catch (Exception e) {
            log.error("Failed to fetch unsettled transactions from transaction-service", e);
            batch.setStatus(SettlementStatus.FAILED);
            batch.setEndTime(LocalDateTime.now());
            batchRepo.save(batch);
            throw new SettlementProcessingException("Settlement failed: unable to fetch unsettled transactions", e);
        }

        if (unsettledTxns == null || unsettledTxns.isEmpty()) {
            log.info("No unsettled transactions found for date: {}", today);
            batch.setStatus(SettlementStatus.COMPLETED);
            batch.setEndTime(LocalDateTime.now());
            batch = batchRepo.save(batch);
            return mapToBatchResponse(batch);
        }

        // 4. Group by merchant
        Map<String, List<UnsettledTransactionDto>> merchantGroups = 
                calculationService.groupTransactionsByMerchant(unsettledTxns);

        int totalMerchants = merchantGroups.size();
        int totalTransactions = unsettledTxns.size();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal processedAmount = BigDecimal.ZERO;
        BigDecimal failedAmount = BigDecimal.ZERO;

        List<String> successfullySettledTxnIds = new ArrayList<>();
        boolean hasFailures = false;

        // 5. Process settlements per merchant
        for (Map.Entry<String, List<UnsettledTransactionDto>> entry : merchantGroups.entrySet()) {
            String merchantUpiId = entry.getKey();
            List<UnsettledTransactionDto> txns = entry.getValue();

            BigDecimal merchantGross = txns.stream()
                    .map(UnsettledTransactionDto::amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalAmount = totalAmount.add(merchantGross);

            String settlementId = "SET" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

            // Fetch merchant bank details
            MerchantBankDetailsDto bankDetails = null;
            try {
                bankDetails = bankGatewayClient.getMerchantBankDetails(serviceKey, merchantUpiId);
            } catch (Exception e) {
                log.error("Failed to fetch bank details for merchant: {}", merchantUpiId, e);
            }

            if (bankDetails == null) {
                // Mark merchant settlement as failed due to missing bank details
                MerchantSettlement failedSettlement = createFailedSettlement(
                        settlementId, batchId, merchantUpiId, merchantGross, today, 
                        "Merchant primary bank account not found"
                );
                failedAmount = failedAmount.add(merchantGross);
                hasFailures = true;

                // Save linking transactions as failed/linked to failed settlement
                linkTransactions(settlementId, txns);
                continue;
            }

            // Calculate fees & net
            BigDecimal platformFee = calculationService.calculatePlatformFee(merchantGross);
            BigDecimal gst = calculationService.calculateGst(platformFee);
            BigDecimal net = calculationService.calculateNetAmount(merchantGross, platformFee, gst);

            // Create initial settlement record
            MerchantSettlement settlement = MerchantSettlement.builder()
                    .settlementId(settlementId)
                    .batchId(batchId)
                    .merchantUpiId(merchantUpiId)
                    .merchantBankAccount(bankDetails.encryptedAccountNumber())
                    .merchantIfsc(bankDetails.ifscCode())
                    .merchantBankName(bankDetails.bankName())
                    .transactionCount(txns.size())
                    .grossAmount(merchantGross)
                    .platformFee(platformFee)
                    .gstOnFee(gst)
                    .netAmount(net)
                    .settlementDate(today)
                    .retryCount(0)
                    .build();

            // Link transactions
            linkTransactions(settlementId, txns);

            // Minimum amount check
            if (net.compareTo(minSettleAmount) < 0) {
                log.warn("Net amount {} for merchant {} is below minimum settlement threshold: {}", net, merchantUpiId, minSettleAmount);
                settlement.setStatus(SettlementStatus.ON_HOLD);
                settlement.setFailureReason("Amount below minimum settlement limit: " + minSettleAmount);
                merchantSettlementRepo.save(settlement);
                failedAmount = failedAmount.add(merchantGross);
                continue;
            }

            // Determine routing mode
            SettlementMode mode = calculationService.determineSettlementMode(net);
            settlement.setMode(mode);
            settlement.setStatus(SettlementStatus.PROCESSING);
            settlement = merchantSettlementRepo.save(settlement);

            // Trigger Bank Transfer
            try {
                String plainAccount = SettlementEncryptionUtil.decrypt(bankDetails.encryptedAccountNumber());
                String bankRef = bankTransferService.initiateTransfer(settlement, plainAccount);
                
                settlement.setStatus(SettlementStatus.COMPLETED);
                settlement.setBankReferenceNumber(bankRef);
                settlement.setCompletedAt(LocalDateTime.now());
                merchantSettlementRepo.save(settlement);

                processedAmount = processedAmount.add(net);
                txns.forEach(t -> successfullySettledTxnIds.add(t.transactionId()));

            } catch (Exception e) {
                log.error("Bank transfer failed for merchant settlement: {}", settlementId, e);
                settlement.setStatus(SettlementStatus.FAILED);
                settlement.setFailureReason("Bank Gateway Error: " + e.getMessage());
                merchantSettlementRepo.save(settlement);
                failedAmount = failedAmount.add(merchantGross);
                hasFailures = true;
            }
        }

        // 6. Update Batch stats
        batch.setTotalMerchants(totalMerchants);
        batch.setTotalTransactions(totalTransactions);
        batch.setTotalAmount(totalAmount);
        batch.setProcessedAmount(processedAmount);
        batch.setFailedAmount(failedAmount);
        batch.setStatus(hasFailures ? SettlementStatus.PARTIALLY_FAILED : SettlementStatus.COMPLETED);
        batch.setEndTime(LocalDateTime.now());
        batch = batchRepo.save(batch);

        // 7. Update transaction-service for successfully settled transactions
        if (!successfullySettledTxnIds.isEmpty()) {
            try {
                log.info("Marking {} transactions as settled in transaction-service", successfullySettledTxnIds.size());
                transactionServiceClient.markTransactionsSettled(serviceKey, batchId, successfullySettledTxnIds);
            } catch (Exception e) {
                log.error("Failed to mark transactions as settled in transaction-service", e);
                // In production, we would queue this via a retry mechanism or Kafka.
            }
        }

        log.info("Completed daily settlement batch: {} | status: {}", batchId, batch.getStatus());
        return mapToBatchResponse(batch);
    }

    @Transactional
    public void retryFailedSettlements() {
        log.info("Starting background task: Retry failed merchant settlements");
        List<MerchantSettlement> failedSettlements = merchantSettlementRepo.findByStatusAndRetryCountLessThan(
                SettlementStatus.FAILED, maxRetryCount);

        log.info("Found {} failed settlements eligible for retry", failedSettlements.size());

        for (MerchantSettlement settlement : failedSettlements) {
            try {
                settlement.setRetryCount(settlement.getRetryCount() + 1);
                log.info("Retrying settlement ID: {} | Attempt: {}", settlement.getSettlementId(), settlement.getRetryCount());

                // Fetch fresh bank details
                MerchantBankDetailsDto bankDetails = bankGatewayClient.getMerchantBankDetails(
                        serviceKey, settlement.getMerchantUpiId());

                if (bankDetails == null) {
                    throw new MerchantBankDetailsNotFoundException("Bank details not found during retry");
                }

                // Update details in case they changed
                settlement.setMerchantBankAccount(bankDetails.encryptedAccountNumber());
                settlement.setMerchantIfsc(bankDetails.ifscCode());
                settlement.setMerchantBankName(bankDetails.bankName());

                String plainAccount = SettlementEncryptionUtil.decrypt(bankDetails.encryptedAccountNumber());
                String bankRef = bankTransferService.initiateTransfer(settlement, plainAccount);

                settlement.setStatus(SettlementStatus.COMPLETED);
                settlement.setBankReferenceNumber(bankRef);
                settlement.setCompletedAt(LocalDateTime.now());
                settlement.setFailureReason(null);
                merchantSettlementRepo.save(settlement);

                // Mark transactions settled in transaction-service
                List<SettlementTransaction> linkedTxns = settlementTxnRepo.findBySettlementId(settlement.getSettlementId());
                List<String> txnIds = linkedTxns.stream()
                        .map(SettlementTransaction::getOriginalTransactionId)
                        .collect(Collectors.toList());

                if (!txnIds.isEmpty()) {
                    transactionServiceClient.markTransactionsSettled(serviceKey, settlement.getBatchId(), txnIds);
                }

                // Adjust aggregates in parent batch
                batchRepo.findByBatchId(settlement.getBatchId()).ifPresent(batch -> {
                    batch.setProcessedAmount(batch.getProcessedAmount().add(settlement.getNetAmount()));
                    batch.setFailedAmount(batch.getFailedAmount().subtract(settlement.getGrossAmount()));
                    
                    // Check if parent batch has any more active failures
                    List<MerchantSettlement> remainingFailed = merchantSettlementRepo.findByBatchIdAndStatus(
                            batch.getBatchId(), SettlementStatus.FAILED);
                    if (remainingFailed.isEmpty()) {
                        batch.setStatus(SettlementStatus.COMPLETED);
                    }
                    batchRepo.save(batch);
                });

                log.info("Settlement ID: {} retried and COMPLETED successfully", settlement.getSettlementId());

            } catch (Exception e) {
                log.error("Retry failed for settlement ID: {}", settlement.getSettlementId(), e);
                settlement.setFailureReason("Retry failure: " + e.getMessage());
                merchantSettlementRepo.save(settlement);
            }
        }
    }

    public SettlementReportResponse getSettlementReport(String batchId) {
        SettlementBatch batch = batchRepo.findByBatchId(batchId)
                .orElseThrow(() -> new SettlementNotFoundException("Settlement batch not found: " + batchId));

        List<MerchantSettlement> settlements = merchantSettlementRepo.findByBatchId(batchId);

        long completedCount = settlements.stream()
                .filter(s -> s.getStatus() == SettlementStatus.COMPLETED)
                .count();

        double successRate = settlements.isEmpty() ? 0.0 : 
                ((double) completedCount / settlements.size()) * 100;

        List<MerchantSettlementResponse> msResponses = settlements.stream()
                .map(this::mapToMerchantResponse)
                .collect(Collectors.toList());

        return SettlementReportResponse.builder()
                .batchId(batch.getBatchId())
                .settlementDate(batch.getSettlementDate())
                .totalMerchants(batch.getTotalMerchants())
                .totalAmount(batch.getTotalAmount())
                .processedAmount(batch.getProcessedAmount())
                .failedAmount(batch.getFailedAmount())
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .settlements(msResponses)
                .build();
    }

    public List<MerchantSettlementResponse> getMerchantSettlements(String merchantUpiId) {
        return merchantSettlementRepo.findByMerchantUpiIdOrderByCreatedAtDesc(merchantUpiId).stream()
                .map(this::mapToMerchantResponse)
                .collect(Collectors.toList());
    }

    public SettlementBatchResponse getBatchDetails(String batchId) {
        SettlementBatch batch = batchRepo.findByBatchId(batchId)
                .orElseThrow(() -> new SettlementNotFoundException("Settlement batch not found: " + batchId));
        return mapToBatchResponse(batch);
    }

    public SettlementBatchResponse getBatchByDate(LocalDate date) {
        SettlementBatch batch = batchRepo.findBySettlementDate(date)
                .orElseThrow(() -> new SettlementNotFoundException("Settlement batch not found for date: " + date));
        return mapToBatchResponse(batch);
    }

    // ─── Helper Methods ────────────────────────────────────────────────────────

    private void linkTransactions(String settlementId, List<UnsettledTransactionDto> txns) {
        for (UnsettledTransactionDto txn : txns) {
            BigDecimal rawFee = calculationService.calculatePlatformFee(txn.amount());
            BigDecimal gst = calculationService.calculateGst(rawFee);
            BigDecimal net = txn.amount().subtract(rawFee).subtract(gst);

            SettlementTransaction st = SettlementTransaction.builder()
                    .settlementId(settlementId)
                    .originalTransactionId(txn.transactionId())
                    .merchantUpiId(txn.merchantUpiId())
                    .amount(txn.amount())
                    .platformFee(rawFee.add(gst))
                    .netAmount(net)
                    .transactionDate(txn.completedAt().toLocalDate())
                    .build();
            settlementTxnRepo.save(st);
        }
    }

    private MerchantSettlement createFailedSettlement(
            String settlementId, String batchId, String merchantUpiId, 
            BigDecimal gross, LocalDate date, String reason) {
        MerchantSettlement ms = MerchantSettlement.builder()
                .settlementId(settlementId)
                .batchId(batchId)
                .merchantUpiId(merchantUpiId)
                .merchantBankAccount("N/A")
                .merchantIfsc("N/A")
                .merchantBankName("N/A")
                .transactionCount(0)
                .grossAmount(gross)
                .platformFee(BigDecimal.ZERO)
                .gstOnFee(BigDecimal.ZERO)
                .netAmount(BigDecimal.ZERO)
                .mode(SettlementMode.IMPS)
                .status(SettlementStatus.FAILED)
                .failureReason(reason)
                .settlementDate(date)
                .build();
        return merchantSettlementRepo.save(ms);
    }

    private SettlementBatchResponse mapToBatchResponse(SettlementBatch batch) {
        return SettlementBatchResponse.builder()
                .batchId(batch.getBatchId())
                .settlementDate(batch.getSettlementDate())
                .startTime(batch.getStartTime())
                .endTime(batch.getEndTime())
                .totalMerchants(batch.getTotalMerchants())
                .totalTransactions(batch.getTotalTransactions())
                .totalAmount(batch.getTotalAmount())
                .processedAmount(batch.getProcessedAmount())
                .failedAmount(batch.getFailedAmount())
                .status(batch.getStatus())
                .createdAt(batch.getCreatedAt())
                .updatedAt(batch.getUpdatedAt())
                .build();
    }

    private MerchantSettlementResponse mapToMerchantResponse(MerchantSettlement ms) {
        String plainAccount = "N/A";
        if (ms.getMerchantBankAccount() != null && !"N/A".equals(ms.getMerchantBankAccount())) {
            plainAccount = SettlementEncryptionUtil.decrypt(ms.getMerchantBankAccount());
        }
        return MerchantSettlementResponse.builder()
                .settlementId(ms.getSettlementId())
                .batchId(ms.getBatchId())
                .merchantUpiId(ms.getMerchantUpiId())
                .maskedBankAccount(SettlementEncryptionUtil.mask(plainAccount))
                .merchantIfsc(ms.getMerchantIfsc())
                .merchantBankName(ms.getMerchantBankName())
                .transactionCount(ms.getTransactionCount())
                .grossAmount(ms.getGrossAmount())
                .platformFee(ms.getPlatformFee())
                .gstOnFee(ms.getGstOnFee())
                .netAmount(ms.getNetAmount())
                .mode(ms.getMode())
                .status(ms.getStatus())
                .bankReferenceNumber(ms.getBankReferenceNumber())
                .failureReason(ms.getFailureReason())
                .settlementDate(ms.getSettlementDate())
                .retryCount(ms.getRetryCount())
                .createdAt(ms.getCreatedAt())
                .updatedAt(ms.getUpdatedAt())
                .completedAt(ms.getCompletedAt())
                .build();
    }
}
