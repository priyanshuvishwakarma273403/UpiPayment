package com.upimesh.dispute.model.response;

import com.upimesh.dispute.model.enums.DisputeReason;
import com.upimesh.dispute.model.enums.DisputeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeResponse {
    private String disputeId;
    private String transactionId;
    private String userUpiId;
    private String merchantUpiId;
    private BigDecimal amount;
    private DisputeReason reason;
    private DisputeStatus status;
    private String userDescription;
    private String merchantResponse;
    private List<String> evidenceUrls;
    private String resolvedInFavorOf;
    private String resolutionNotes;
    private LocalDateTime raisedAt;
    private LocalDateTime merchantResponseDeadline;
    private LocalDateTime resolvedAt;
}
