package com.fraudService.network.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkNode {

    private String id;
    private String label;
    private NodeType type;
    private Double riskScore;
    private String riskLevel;
    private int connectionCount;
    private int transactionCount;
    private int relatedCaseCount;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
