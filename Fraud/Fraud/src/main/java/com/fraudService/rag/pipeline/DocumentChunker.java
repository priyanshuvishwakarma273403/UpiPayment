package com.fraudService.rag.pipeline;

import com.fraudService.rag.model.PolicyDocumentChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Parses and splits Markdown policy documents into semantic chunks with section header preservation.
 */
@Component
@Slf4j
public class DocumentChunker {

    public List<PolicyDocumentChunk> chunkDocument(String docTitle, String markdownContent) {
        if (markdownContent == null || markdownContent.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<PolicyDocumentChunk> chunks = new ArrayList<>();
        String[] lines = markdownContent.split("\\r?\\n");

        String currentHeader = "General";
        StringBuilder currentContent = new StringBuilder();
        int chunkSeq = 1;

        for (String line : lines) {
            if (line.startsWith("# ")) {
                docTitle = line.substring(2).trim();
            } else if (line.startsWith("## ")) {
                if (currentContent.length() > 0) {
                    chunks.add(buildChunk(docTitle, currentHeader, currentContent.toString().trim(), chunkSeq++));
                    currentContent.setLength(0);
                }
                currentHeader = line.substring(3).trim();
            } else {
                currentContent.append(line).append("\n");
            }
        }

        if (currentContent.length() > 0) {
            chunks.add(buildChunk(docTitle, currentHeader, currentContent.toString().trim(), chunkSeq));
        }

        log.info("Chunked document '{}' into {} semantic policy chunks", docTitle, chunks.size());
        return chunks;
    }

    private PolicyDocumentChunk buildChunk(String docTitle, String sectionHeader, String content, int seq) {
        String chunkId = "CHK-" + docTitle.replaceAll("[^A-Za-z0-9]", "_").toUpperCase() + "-" + seq;
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", docTitle);
        metadata.put("section", sectionHeader);
        metadata.put("seq", seq);

        return PolicyDocumentChunk.builder()
                .chunkId(chunkId)
                .documentTitle(docTitle)
                .sectionHeader(sectionHeader)
                .content(content)
                .metadata(metadata)
                .build();
    }
}
