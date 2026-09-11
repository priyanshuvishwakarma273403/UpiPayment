package com.fraudService.agent.controller;

import com.fraudService.agent.model.AgentReportResponse;
import com.fraudService.agent.service.ControlledFraudAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/fraud/agent")
@RequiredArgsConstructor
@Slf4j
public class FraudAgentController {

    private final ControlledFraudAgentService agentService;

    @PostMapping("/investigate")
    public ResponseEntity<AgentReportResponse> runInvestigation(@RequestBody Map<String, String> payload) {
        String query = payload.getOrDefault("query", "Investigate customer C9281 for possible account takeover.");
        log.info("REST POST /fraud/agent/investigate | query='{}'", query);

        AgentReportResponse response = agentService.runControlledInvestigation(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/investigate/{customerId}")
    public ResponseEntity<AgentReportResponse> investigateCustomer(@PathVariable String customerId) {
        log.info("REST GET /fraud/agent/investigate/{}", customerId);
        String query = "Investigate customer " + customerId + " for possible account takeover.";
        AgentReportResponse response = agentService.runControlledInvestigation(query);
        return ResponseEntity.ok(response);
    }
}
