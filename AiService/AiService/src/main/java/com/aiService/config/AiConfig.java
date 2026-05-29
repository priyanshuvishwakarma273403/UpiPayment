package com.aiService.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ================================================================
 * AI Service Config - Spring AI ChatClient
 * ================================================================
 * ChatClient: Spring AI ka main interface AI models ke saath interact karne ke liye.
 * OpenAiChatModel: OpenAI GPT models use karta hai.
 *
 * Production alternatives:
 * - Anthropic Claude: spring-ai-anthropic-spring-boot-starter
 * - Google Gemini:    spring-ai-vertex-ai-gemini-spring-boot-starter
 * - Local Ollama:     spring-ai-ollama-spring-boot-starter (free, local)
 *
 * API Key: application.yml mein spring.ai.openai.api-key set karo
 * ================================================================
 */
@Configuration
public class AiConfig {

    /**
     * ChatClient bean - AiService mein inject hoga
     * Builder pattern se default configuration set kar sakte hain
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are a helpful AI assistant for UPI Payment Mesh.
                        Always be concise, friendly and security-conscious.
                        """)
                .build();
    }
}

