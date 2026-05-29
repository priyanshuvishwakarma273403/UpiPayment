package com.syncService.Sync.contoller;

import com.syncService.Sync.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ================================================================
 * Sync Controller - REST Endpoints
 * ================================================================
 * POST /sync/process  -> Manually trigger offline payment sync
 * GET  /sync/status   -> Current sync status
 * ================================================================
 */
@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sync", description = "Offline payment sync APIs")
public class SyncController {

    private final SyncService syncService;

    /**
     * POST /sync/process
     * Internet wapas aane par offline payments sync karo
     */
    @PostMapping("/process")
    @Operation(
            summary = "Process offline pending payments",
            description = "Syncs all PENDING_SYNC payments for the current user when internet is restored."
    )
    public ResponseEntity<Map<String, Object>> processPendingSync(
            @RequestHeader("X-User-Id") String userIdHeader) {

        Long userId = Long.parseLong(userIdHeader);
        log.info("Manual sync triggered for userId={}", userId);
        Map<String, Object> result = syncService.syncPendingPayments(userId);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /sync/status
     * Sync service health + stats
     */
    @GetMapping("/status")
    @Operation(summary = "Get sync service status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        return ResponseEntity.ok(Map.of(
                "service", "sync-service",
                "status", "UP",
                "message", "Sync service is running. POST /sync/process to trigger manual sync."
        ));
    }
}

