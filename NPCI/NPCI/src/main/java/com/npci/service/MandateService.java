package com.npci.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.npci.model.entity.UpiMandate;
import com.npci.model.enums.MandateStatus;
import com.npci.model.request.MandateCreateRequest;
import com.npci.model.response.MandateResponse;
import com.npci.repository.UpiMandateRepository;
import jakarta.transaction.InvalidTransactionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.TransactionalIdNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MandateService — Handles UPI AutoPay recurring payments.
 *
 * Example: User subscribes to Spotify → mandate created →
 * every month ₹119 auto-debited without user interaction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MandateService {


    private final UpiMandateRepository mandateRepo;
    private final NpciClient npciClient;

    @Transactional
    public MandateResponse createMandate(MandateCreateRequest request) {

        String mandateId = "MND" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 14).toUpperCase();

        // Call NPCI to register mandate (sends OTP to user for consent)
        JsonNode npciResponse = npciClient.createMandate(
                request.getUserUpiId(),
                request.getMerchantUpiId(),
                request.getMaxAmount(),
                request.getFrequency(),
                request.getStartDate(),
                request.getEndDate(),
                request.getPurpose(),
                mandateId
        );

        String responseCode = npciResponse.path("responseCode").asText("");
        String status = npciResponse.path("status").asText("FAILURE");
        String npciMandateId = npciResponse.path("mandateId").asText(null);

        // Mandate starts as CREATED — becomes ACTIVE once user approves via bank OTP
        MandateStatus mandateStatus = ("SUCCESS".equalsIgnoreCase(status) || "00".equals(responseCode))
                ? MandateStatus.CREATED
                : MandateStatus.REVOKED;

        UpiMandate mandate = UpiMandate.builder()
                .mandateId(mandateId)
                .npciMandateId(npciMandateId)
                .userUpiId(request.getUserUpiId())
                .merchantUpiId(request.getMerchantUpiId())
                .merchantName(request.getMerchantName())
                .maxAmount(request.getMaxAmount())
                .frequency(request.getFrequency())
                .executionDay(request.getExecutionDay())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextExecutionDate(calculateNextExecution(request.getStartDate(), request.getFrequency(), request.getExecutionDay()))
                .purpose(request.getPurpose())
                .status(mandateStatus)
                .build();

        mandateRepo.save(mandate);
        log.info("Mandate created | mandateId={} | user={} | status={}",
                mandateId, request.getUserUpiId(), mandateStatus);

        return mapToResponse(mandate);
    }

    @Transactional
    public MandateResponse pauseMandate(String mandateId) throws InvalidTransactionException {
        UpiMandate mandate = getMandateOrThrow(mandateId);

        if (mandate.getStatus() != MandateStatus.ACTIVE) {
            throw new InvalidTransactionException("Only ACTIVE mandates can be paused");
        }

        mandate.setStatus(MandateStatus.PAUSED);
        mandateRepo.save(mandate);
        log.info("Mandate paused | mandateId={}", mandateId);
        return mapToResponse(mandate);
    }

    @Transactional
    public MandateResponse revokeMandate(String mandateId) throws InvalidTransactionException {
        UpiMandate mandate = getMandateOrThrow(mandateId);

        if (mandate.getStatus() == MandateStatus.REVOKED) {
            throw new InvalidTransactionException("Mandate is already revoked");
        }

        mandate.setStatus(MandateStatus.REVOKED);
        mandateRepo.save(mandate);
        log.info("Mandate revoked | mandateId={}", mandateId);
        return mapToResponse(mandate);
    }

    public List<MandateResponse> getUserMandates(String userUpiId) {
        return mandateRepo.findByUserUpiIdAndStatus(userUpiId, MandateStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Called by scheduler — executes all mandates due today
     */
    @Transactional
    public void executeDueMandates() {
        List<UpiMandate> dueMandates = mandateRepo.findMandatesDueForExecution(LocalDate.now());
        log.info("Executing {} mandates due today", dueMandates.size());

        for (UpiMandate mandate : dueMandates) {
            try {
                executeSingleMandate(mandate);
            } catch (Exception e) {
                log.error("Mandate execution failed | mandateId={} | error={}",
                        mandate.getMandateId(), e.getMessage());
                mandate.setFailedAttempts(mandate.getFailedAttempts() + 1);

                // After 3 failures, revoke the mandate
                if (mandate.getFailedAttempts() >= 3) {
                    mandate.setStatus(MandateStatus.REVOKED);
                    log.warn("Mandate revoked after 3 failures | mandateId={}", mandate.getMandateId());
                }
                mandateRepo.save(mandate);
            }
        }
    }

    private void executeSingleMandate(UpiMandate mandate) {
        log.info("Executing mandate | mandateId={} | amount={}", mandate.getMandateId(), mandate.getMaxAmount());

        // Auto-debit via NPCI
        npciClient.initiateTransaction(
                mandate.getUserUpiId(),
                mandate.getMerchantUpiId(),
                mandate.getCurrentAmount() != null ? mandate.getCurrentAmount() : mandate.getMaxAmount(),
                "MND-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "Auto-debit: " + mandate.getMerchantName(),
                "MANDATE_HASH"  // In prod, mandate doesn't need MPIN — pre-authorized
        );

        // Update next execution date
        mandate.setLastExecutedDate(LocalDate.now());
        mandate.setNextExecutionDate(
                calculateNextExecution(LocalDate.now(), mandate.getFrequency(), mandate.getExecutionDay()));
        mandate.setFailedAttempts(0);
        mandateRepo.save(mandate);
    }

    private LocalDate calculateNextExecution(LocalDate from, String frequency, Integer executionDay) {
        return switch (frequency) {
            case "DAILY"   -> from.plusDays(1);
            case "WEEKLY"  -> from.plusWeeks(1);
            case "MONTHLY" -> executionDay != null
                    ? from.plusMonths(1).withDayOfMonth(Math.min(executionDay, from.plusMonths(1).lengthOfMonth()))
                    : from.plusMonths(1);
            default -> from.plusMonths(1);
        };
    }

    private UpiMandate getMandateOrThrow(String mandateId) {
        return mandateRepo.findByMandateId(mandateId)
                .orElseThrow(() -> new TransactionalIdNotFoundException("Mandate not found: " + mandateId));
    }

    private MandateResponse mapToResponse(UpiMandate m) {
        return MandateResponse.builder()
                .mandateId(m.getMandateId())
                .npciMandateId(m.getNpciMandateId())
                .userUpiId(m.getUserUpiId())
                .merchantUpiId(m.getMerchantUpiId())
                .merchantName(m.getMerchantName())
                .maxAmount(m.getMaxAmount())
                .frequency(m.getFrequency())
                .executionDay(m.getExecutionDay())
                .startDate(m.getStartDate())
                .endDate(m.getEndDate())
                .nextExecutionDate(m.getNextExecutionDate())
                .status(m.getStatus())
                .purpose(m.getPurpose())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
