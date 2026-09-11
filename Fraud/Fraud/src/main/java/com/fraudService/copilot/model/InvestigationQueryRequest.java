package com.fraudService.copilot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationQueryRequest {

    private String query;
    private String entityId;
    private String entityType; // TRANSACTION, CUSTOMER
    private String analystId;

}
