package com.fraudService.network.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkRelationship {

    private String id;
    private String sourceId;
    private String targetId;
    private RelationshipType type;
    private Double weight;
    private LocalDateTime timestamp;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
