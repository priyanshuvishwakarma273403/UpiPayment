package com.fraudService.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {

    private String query;
    private long totalHits;
    private int page;
    private int pageSize;
    private long executionTimeMs;

    @Builder.Default
    private List<InvestigationSearchDocument> documents = new ArrayList<>();

    @Builder.Default
    private Map<String, Long> riskLevelFacets = new HashMap<>();

    @Builder.Default
    private Map<String, Long> statusFacets = new HashMap<>();

    @Builder.Default
    private Map<String, Long> fraudTypeFacets = new HashMap<>();
}
