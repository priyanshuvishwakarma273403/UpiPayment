package com.fraudService.network.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkGraph {

    private String rootEntityId;
    private NodeType rootEntityType;
    private int traversalDepth;

    @Builder.Default
    private List<NetworkNode> nodes = new ArrayList<>();

    @Builder.Default
    private List<NetworkRelationship> relationships = new ArrayList<>();

    private Double clusterRiskScore;
    private String networkSummary;
    private List<String> empiricalEvidence;
}
