package com.fraudService.rag;

import com.fraudService.rag.model.GroundedRagResponse;
import com.fraudService.rag.model.PolicyDocumentChunk;
import com.fraudService.rag.model.PolicyQueryResult;
import com.fraudService.rag.pipeline.DocumentChunker;
import com.fraudService.rag.service.FraudPolicyRagService;
import com.fraudService.rag.store.QdrantVectorStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FraudPolicyRagServiceTest {

    private DocumentChunker chunker;
    private QdrantVectorStore vectorStore;
    private FraudPolicyRagService ragService;

    @BeforeEach
    void setUp() {
        chunker = new DocumentChunker();
        vectorStore = new QdrantVectorStore();
        ragService = new FraudPolicyRagService(chunker, vectorStore);
        ragService.ingestDefaultPolicies();
    }

    @Test
    @DisplayName("Vector Similarity Search retrieves relevant ATO policy chunk for query")
    void testPolicySimilaritySearch() {
        String query = "account takeover device change";
        List<PolicyQueryResult> results = ragService.searchPolicy(query, 3);

        assertFalse(results.isEmpty(), "Vector search should return policy matches");
        PolicyQueryResult topMatch = results.get(0);
        assertTrue(topMatch.getDocumentTitle().contains("Takeover"), "Top match must be Account Takeover Policy");
        assertTrue(topMatch.getSimilarityScore() > 0.0, "Similarity score must be positive");
    }

    @Test
    @DisplayName("Grounded RAG Query generates complete 4-layer response")
    void testGroundedRagQuerySuccess() {
        String query = "account takeover device change";
        GroundedRagResponse response = ragService.queryGroundedRag(query, null, null);

        assertNotNull(response);
        assertTrue(response.isPolicyFound(), "Policy must be marked as found");
        assertFalse(response.getObservedData().isEmpty(), "Layer 1: Observed Data must be present");
        assertFalse(response.getRetrievedPolicy().isEmpty(), "Layer 2: Retrieved Policy must be present");
        assertFalse(response.getModelPrediction().isEmpty(), "Layer 3: Model Prediction must be present");
        assertFalse(response.getAiRecommendation().isEmpty(), "Layer 4: AI Recommendation must be present");
        assertNotNull(response.getFormattedReport(), "Formatted report must be generated");
    }

    @Test
    @DisplayName("Anti-Fabrication Rule: Unmatched query returns 'No relevant policy document found for query.'")
    void testAntiFabricationRule() {
        String unmatchedQuery = "xyz999unmatched unknown directive";
        GroundedRagResponse response = ragService.queryGroundedRag(unmatchedQuery, null, null);

        assertNotNull(response);
        assertFalse(response.isPolicyFound(), "Policy found should be false for unmatched query");
        assertEquals("No relevant policy document found for query.", response.getRetrievedPolicy().get(0));
    }

    @Test
    @DisplayName("Document Chunker splits markdown headers into semantic chunks")
    void testDocumentChunkingAndIngestion() {
        String markdown = """
                # Sample Policy
                ## Section 1: Introduction
                This is section 1 content.
                ## Section 2: Requirements
                This is section 2 content.
                """;

        List<PolicyDocumentChunk> chunks = chunker.chunkDocument("Sample Policy", markdown);
        assertEquals(2, chunks.size(), "Markdown should produce 2 semantic section chunks");
        assertEquals("Section 1: Introduction", chunks.get(0).getSectionHeader());
        assertEquals("Section 2: Requirements", chunks.get(1).getSectionHeader());
    }
}
