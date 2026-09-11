package com.fraudService.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyQueryResult {

    private String chunkId;
    private String documentTitle;
    private String sectionHeader;
    private String snippet;
    private double similarityScore;

}
