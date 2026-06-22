package com.upimesh.aml.controller;

import com.upimesh.aml.exception.ScreeningNotFoundException;
import com.upimesh.aml.model.entity.AmlAlert;
import com.upimesh.aml.model.entity.AmlScreeningResult;
import com.upimesh.aml.model.entity.WatchlistEntry;
import com.upimesh.aml.model.request.AmlCheckRequest;
import com.upimesh.aml.model.response.AmlCheckResponse;
import com.upimesh.aml.model.response.ApiResponse;
import com.upimesh.aml.repository.AmlScreeningResultRepository;
import com.upimesh.aml.service.AmlService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aml")
@RequiredArgsConstructor
public class AmlController {

    private final AmlService amlService;
    private final AmlScreeningResultRepository screeningRepository;

    @PostMapping("/screen")
    public ResponseEntity<ApiResponse<AmlCheckResponse>> screenTransaction(
            @Valid @RequestBody AmlCheckRequest request) {
        AmlCheckResponse response = amlService.screenTransaction(request);
        return ResponseEntity.ok(ApiResponse.success("AML check executed successfully", response));
    }

    @GetMapping("/alerts/{userUpiId}")
    public ResponseEntity<ApiResponse<List<AmlAlert>>> getAlerts(
            @PathVariable String userUpiId) {
        List<AmlAlert> alerts = amlService.getAlerts(userUpiId);
        return ResponseEntity.ok(ApiResponse.success("Alerts retrieved successfully", alerts));
    }

    @PutMapping("/alert/{alertId}/resolve")
    public ResponseEntity<ApiResponse<AmlAlert>> resolveAlert(
            @PathVariable String alertId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String resolvedBy,
            @RequestBody(required = false) ResolveRequest body) {
        
        String finalNotes = notes;
        String finalResolvedBy = resolvedBy;
        if (body != null) {
            if (body.getNotes() != null) finalNotes = body.getNotes();
            if (body.getResolvedBy() != null) finalResolvedBy = body.getResolvedBy();
        }
        
        if (finalNotes == null || finalNotes.trim().isEmpty()) {
            throw new IllegalArgumentException("Resolution notes are required");
        }
        if (finalResolvedBy == null || finalResolvedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Resolved by identifier is required");
        }

        AmlAlert alert = amlService.resolveAlert(alertId, finalNotes, finalResolvedBy);
        return ResponseEntity.ok(ApiResponse.success("Alert resolved successfully", alert));
    }

    @PutMapping("/alert/{alertId}/escalate")
    public ResponseEntity<ApiResponse<AmlAlert>> escalateAlert(
            @PathVariable String alertId,
            @RequestParam(required = false) String assignedTo,
            @RequestBody(required = false) EscalateRequest body) {

        String finalAssignedTo = assignedTo;
        if (body != null && body.getAssignedTo() != null) {
            finalAssignedTo = body.getAssignedTo();
        }

        if (finalAssignedTo == null || finalAssignedTo.trim().isEmpty()) {
            throw new IllegalArgumentException("Assigned to field is required");
        }

        AmlAlert alert = amlService.escalateAlert(alertId, finalAssignedTo);
        return ResponseEntity.ok(ApiResponse.success("Alert escalated successfully", alert));
    }

    @PostMapping("/watchlist")
    public ResponseEntity<ApiResponse<WatchlistEntry>> addToWatchlist(
            @RequestBody WatchlistEntry entry) {
        if (entry.getName() == null || entry.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Watchlist entry name is required");
        }
        if (entry.getEntityType() == null || entry.getEntityType().trim().isEmpty()) {
            throw new IllegalArgumentException("Watchlist entityType (e.g. PEP, SANCTIONS) is required");
        }
        if (entry.getSource() == null || entry.getSource().trim().isEmpty()) {
            throw new IllegalArgumentException("Watchlist source (e.g. OFAC, UN) is required");
        }

        WatchlistEntry saved = amlService.addToWatchlist(entry);
        return ResponseEntity.ok(ApiResponse.success("Added to watchlist successfully", saved));
    }

    @GetMapping("/screening/{transactionId}")
    public ResponseEntity<ApiResponse<AmlScreeningResult>> getScreeningResult(
            @PathVariable String transactionId) {
        AmlScreeningResult result = screeningRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ScreeningNotFoundException("Screening result not found for transaction: " + transactionId));
        return ResponseEntity.ok(ApiResponse.success("Screening result retrieved successfully", result));
    }

    @Data
    public static class ResolveRequest {
        private String notes;
        private String resolvedBy;
    }

    @Data
    public static class EscalateRequest {
        private String assignedTo;
    }
}
