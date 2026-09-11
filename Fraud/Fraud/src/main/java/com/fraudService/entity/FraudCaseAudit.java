package com.fraudService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "fraud_case_audits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudCaseAudit {

    @Id
    private String id;

    @Indexed
    private String caseId;

    // Actor: User/Analyst ID performing action
    private String actor;

    // Action: CREATE, ASSIGN, COMMENT, ESCALATE, MARK_FRAUD, MARK_FALSE_POSITIVE, RESOLVE
    private String action;

    @Indexed
    private LocalDateTime timestamp;

    // Target entity type (e.g. FraudCase)
    private String entity;

    private String oldState;

    private String newState;

    private String details;
}
