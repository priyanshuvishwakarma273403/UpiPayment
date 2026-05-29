package com.aiService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Indexed;

import java.lang.annotation.Documented;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ================================================================
 * ChatHistory - MongoDB Collection: chat_history
 * ================================================================
 * AI chatbot ke saare conversations yahan store hote hain.
 * Har user ka separate chat history hota hai.
 * Context window ke liye last N messages use hote hain.
 * ================================================================
 */
@Document(collection = "chat_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistory {

    @Id
    private String id;

    private Long userId;

    private String sessionId;   // Ek conversation session ka ID

    // Messages list: [{role: "user"/"assistant", content: "..."}]
    private List<ChatMessage> messages;

    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;      // "user" ya "assistant"
        private String content;
        private LocalDateTime timestamp;
    }
}
