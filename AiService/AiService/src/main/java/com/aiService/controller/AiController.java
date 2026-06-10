package com.aiService.controller;

import com.aiService.dto.request.ChatRequest;
import com.aiService.dto.request.ExpenseAnalysisRequest;
import com.aiService.dto.response.ChatResponse;
import com.aiService.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
        * ================================================================
        * AI Controller - REST Endpoints
 * ================================================================
         * POST /ai/chat             -> Payment chatbot
 * POST /ai/expense-analysis -> Spending insights
 * POST /ai/fraud-explain    -> Fraud decision explanation
 * ================================================================
         */

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Service", description = "AI-powered chatbot, expense analysis, and fraud explanation")
public class AiController {

    private final AiService aiService;

    /**
     * POST /ai/chat
     * UPI Payment chatbot - user payment queries handle karo
     */
    @PostMapping("/chat")
    @Operation(
            summary = "Payment AI Chatbot",
            description = "Chat with AI assistant for payment queries. Maintains conversation context.")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {

        Long userId = Long.parseLong(userIdHeader);
        ChatResponse response = aiService.chat(request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /ai/expense-analysis
     * User ki spending patterns ka AI analysis
     */
    @PostMapping("/expense-analysis")
    @Operation(
            summary = "AI Expense Analysis",
            description = "Analyze spending patterns and get AI-powered savings recommendations.")
    public ResponseEntity<Map<String, Object>> analyzeExpenses(
            @Valid @RequestBody ExpenseAnalysisRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {

        // Ensure user sirf apna analysis dekh sake
        request.setUserId(Long.parseLong(userIdHeader));
        Map<String, Object> analysis = aiService.analyzeExpenses(request);
        return ResponseEntity.ok(analysis);
    }

    /**
     * POST /ai/fraud-explain
     * Fraud decision ka user-friendly explanation
     * Body: { "paymentId": "PAY-xxx", "fraudDecision": "BLOCKED", "reasons": "...", "riskScore": 0.85 }
     */
    @PostMapping("/fraud-explain")
    @Operation(
            summary = "Explain Fraud Decision",
            description = "Get AI-powered plain-language explanation of why a payment was blocked/flagged.")
    public ResponseEntity<Map<String, Object>> explainFraud(
            @RequestBody Map<String, Object> request) {

        String paymentId    = (String) request.get("paymentId");
        String decision     = (String) request.get("fraudDecision");
        String reasons      = (String) request.getOrDefault("reasons", "Suspicious activity detected");
        double riskScore    = Double.parseDouble(request.getOrDefault("riskScore", 0.0).toString());

        Map<String, Object> explanation = aiService.explainFraudDecision(
                paymentId, decision, reasons, riskScore);
        return ResponseEntity.ok(explanation);
    }
}
