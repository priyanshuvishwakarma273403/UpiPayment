package com.fraudService.copilot.controller;

import com.fraudService.copilot.model.InvestigationQueryRequest;
import com.fraudService.copilot.model.InvestigationSummaryResponse;
import com.fraudService.copilot.service.FraudInvestigationCopilotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fraud/copilot")
@RequiredArgsConstructor
@Slf4j
public class FraudCopilotController {

    private final FraudInvestigationCopilotService copilotService;

    @PostMapping("/investigate")
    public ResponseEntity<InvestigationSummaryResponse> investigate(@RequestBody InvestigationQueryRequest request) {
        log.info("REST POST /fraud/copilot/investigate | query='{}'", request.getQuery());
        InvestigationSummaryResponse response = copilotService.investigate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/investigate/{entityId}")
    public ResponseEntity<InvestigationSummaryResponse> investigateEntity(@PathVariable String entityId) {
        log.info("REST GET /fraud/copilot/investigate/{}", entityId);
        InvestigationQueryRequest request = InvestigationQueryRequest.builder()
                .query("Investigate entity " + entityId)
                .entityId(entityId)
                .build();
        InvestigationSummaryResponse response = copilotService.investigate(request);
        return ResponseEntity.ok(response);
    }
}
