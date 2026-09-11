package com.fraudService.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDocumentChunk {

    private String chunkId;
    private String documentTitle;
    private String sectionHeader;
    private String content;
    private double[] embedding;
    private Map<String, Object> metadata;

}
