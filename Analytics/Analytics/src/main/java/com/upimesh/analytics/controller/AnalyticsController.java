package com.upimesh.analytics.controller;

import com.upimesh.analytics.dto.DashboardSummaryDto;
import com.upimesh.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/analytics", "/analytic"})
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        log.info("REST request to get analytics dashboard summary");
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }
}
