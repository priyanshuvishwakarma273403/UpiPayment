package com.aiService.service;

import com.aiService.dto.request.ChatRequest;
import com.aiService.dto.request.ExpenseAnalysisRequest;
import com.aiService.dto.response.ChatResponse;
import com.aiService.entity.ChatHistory;
import com.aiService.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final ChatClient chatClient;
    private final ChatHistoryRepository chatHistoryRepository;

    private static final String SYSTEM_PROMPT = """
            You are a helpful AI assistant for UPI Payment Mesh - a digital payment system.
            You help users with:
            - Payment queries and issues
            - Understanding transaction history
            - Fraud alerts and security tips
            - Expense management advice
            - UPI and digital payment guidance

            Keep responses concise, friendly, and simple.
            Respond in the same language as the user.
            """;

    // ============================================================
    // CHATBOT
    // ============================================================

    public ChatResponse chat(ChatRequest request, Long userId) {

        log.info("Chat request from userId={}, sessionId={}",
                userId, request.getSessionId());

        String sessionId = request.getSessionId() != null
                && !request.getSessionId().isBlank()
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        ChatHistory chatHistory = chatHistoryRepository
                .findByUserIdAndSessionId(userId, sessionId)
                .orElseGet(() -> ChatHistory.builder()
                        .userId(userId)
                        .sessionId(sessionId)
                        .messages(new ArrayList<>())
                        .createdAt(LocalDateTime.now())
                        .build());

        List<Message> messages =
                buildMessageHistory(chatHistory, request.getMessage());

        String aiResponse;

        try {

            Prompt prompt = new Prompt(messages);

            aiResponse = chatClient.prompt(prompt)
                    .call()
                    .content();

        } catch (Exception e) {

            log.error("AI call failed: {}", e.getMessage());

            aiResponse = """
                    Sorry, I am unable to process your request right now.
                    Please try again later.
                    """;
        }

        // Save User Message
        chatHistory.getMessages().add(
                ChatHistory.ChatMessage.builder()
                        .role("user")
                        .content(request.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        // Save AI Message
        chatHistory.getMessages().add(
                ChatHistory.ChatMessage.builder()
                        .role("assistant")
                        .content(aiResponse)
                        .timestamp(LocalDateTime.now())
                        .build()
        );

        chatHistory.setLastMessageAt(LocalDateTime.now());

        chatHistoryRepository.save(chatHistory);

        return ChatResponse.builder()
                .sessionId(sessionId)
                .userId(userId)
                .userMessage(request.getMessage())
                .aiResponse(aiResponse)
                .model("gpt-3.5-turbo")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ============================================================
    // EXPENSE ANALYSIS
    // ============================================================

    public Map<String, Object> analyzeExpenses(
            ExpenseAnalysisRequest request) {

        String transactionSummary = """
                Last 30 days transactions:
                - Food: ₹4500
                - Shopping: ₹8200
                - Travel: ₹2100
                - Entertainment: ₹1800
                - Utilities: ₹3200
                Total Spend: ₹19800
                """;

        String prompt = """
                Analyze user expenses and provide:
                1. Spending insights
                2. Saving recommendations
                3. Budget tips

                Transaction Summary:
                """ + transactionSummary;

        String analysis;

        try {

            analysis = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(prompt)
                    .call()
                    .content();

        } catch (Exception e) {

            log.error("Expense analysis failed: {}", e.getMessage());

            analysis = "Unable to generate analysis right now.";
        }

        return Map.of(
                "userId", request.getUserId(),
                "analysis", analysis,
                "generatedAt", LocalDateTime.now().toString()
        );
    }

    // ============================================================
    // FRAUD EXPLANATION
    // ============================================================

    public Map<String, Object> explainFraudDecision(
            String paymentId,
            String fraudDecision,
            String fraudReasons,
            double riskScore) {

        String prompt = String.format("""
                Explain this fraud decision in simple language.

                Payment ID: %s
                Decision: %s
                Risk Score: %.2f
                Reasons: %s
                """,
                paymentId,
                fraudDecision,
                riskScore,
                fraudReasons
        );

        String explanation;

        try {

            explanation = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(prompt)
                    .call()
                    .content();

        } catch (Exception e) {

            log.error("Fraud explanation failed: {}", e.getMessage());

            explanation = """
                    Your payment was flagged for security reasons.
                    Please contact support.
                    """;
        }

        return Map.of(
                "paymentId", paymentId,
                "decision", fraudDecision,
                "riskScore", riskScore,
                "explanation", explanation,
                "generatedAt", LocalDateTime.now().toString()
        );
    }

    // ============================================================
    // HELPER METHOD
    // ============================================================

    private List<Message> buildMessageHistory(
            ChatHistory history,
            String currentUserMessage) {

        List<Message> messages = new ArrayList<>();

        messages.add(new SystemMessage(SYSTEM_PROMPT));

        List<ChatHistory.ChatMessage> historyMessages =
                history.getMessages();

        int startIdx = Math.max(0, historyMessages.size() - 10);

        for (int i = startIdx; i < historyMessages.size(); i++) {

            ChatHistory.ChatMessage msg = historyMessages.get(i);

            if ("user".equals(msg.getRole())) {

                messages.add(new UserMessage(msg.getContent()));

            } else {

                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        messages.add(new UserMessage(currentUserMessage));

        return messages;
    }
}