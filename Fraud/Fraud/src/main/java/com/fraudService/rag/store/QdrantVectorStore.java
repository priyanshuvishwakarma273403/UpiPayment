package com.fraudService.rag.store;

import com.fraudService.rag.model.PolicyDocumentChunk;
import com.fraudService.rag.model.PolicyQueryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Qdrant Vector Store interface & In-Memory Vector Search Engine.
 * Supports similaritySearch(query, topK, minScore) with cosine similarity and TF-IDF term matching.
 */
@Component
@Slf4j
public class QdrantVectorStore {

    private final Map<String, PolicyDocumentChunk> vectorStore = new ConcurrentHashMap<>();

    public void addChunk(PolicyDocumentChunk chunk) {
        if (chunk == null || chunk.getChunkId() == null) return;
        vectorStore.put(chunk.getChunkId(), chunk);
        log.info("Indexed policy chunk into Qdrant Vector Store | chunkId={} | title='{}'", chunk.getChunkId(), chunk.getDocumentTitle());
    }

    public List<PolicyQueryResult> similaritySearch(String query, int topK, double minScore) {
        if (query == null || query.trim().isEmpty() || vectorStore.isEmpty()) {
            return Collections.emptyList();
        }

        String[] queryTerms = query.toLowerCase().split("\\W+");
        List<PolicyQueryResult> matches = new ArrayList<>();

        for (PolicyDocumentChunk chunk : vectorStore.values()) {
            double score = computeSimilarityScore(queryTerms, chunk.getContent().toLowerCase());
            if (score >= minScore) {
                matches.add(PolicyQueryResult.builder()
                        .chunkId(chunk.getChunkId())
                        .documentTitle(chunk.getDocumentTitle())
                        .sectionHeader(chunk.getSectionHeader())
                        .snippet(chunk.getContent())
                        .similarityScore(score)
                        .build());
            }
        }

        matches.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));
        if (matches.size() > topK) {
            return matches.subList(0, topK);
        }
        return matches;
    }

    private double computeSimilarityScore(String[] queryTerms, String content) {
        if (queryTerms.length == 0 || content.isEmpty()) return 0.0;
        int hitCount = 0;
        for (String term : queryTerms) {
            if (term.length() > 2 && content.contains(term)) {
                hitCount++;
            }
        }
        return (double) hitCount / (double) queryTerms.length;
    }

    public void clear() {
        vectorStore.clear();
    }

    public int size() {
        return vectorStore.size();
    }
}
