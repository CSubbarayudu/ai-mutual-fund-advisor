package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.ApiResponse;
import com.nihilent.mutualfund.advisor.dto.request.MarketEventIngestRequest;
import com.nihilent.mutualfund.advisor.dto.request.McpNewsItem;
import com.nihilent.mutualfund.advisor.dto.response.MarketEventIngestResponse;
import com.nihilent.mutualfund.advisor.service.MarketEventIngestionService;
import com.nihilent.mutualfund.advisor.service.McpNewsSimulatorService;
import com.nihilent.mutualfund.advisor.service.impl.AmfiFundSyncServiceImpl;
import com.nihilent.mutualfund.advisor.service.impl.NewsApiMarketEventPollerImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/market-events")
@RequiredArgsConstructor
@Slf4j
public class MarketEventController {

    private final MarketEventIngestionService ingestionService;
    private final McpNewsSimulatorService mcpNewsSimulatorService;
    private final NewsApiMarketEventPollerImpl newsApiMarketEventPollerImpl;
    private final AmfiFundSyncServiceImpl amfiFundSyncServiceImpl;

    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<Object>> ingestEvent(
            @Valid @RequestBody MarketEventIngestRequest request) {
        log.info("POST /api/v1/market-events/ingest title={}",
            request.getEventTitle());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                "Market event ingested and scoring triggered",
                ingestionService.ingestMarketEvent(request)));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Object>> getActiveEvents() {
        log.info("GET /api/v1/market-events/active");
        return ResponseEntity.ok(ApiResponse.success(
            "Active market events fetched",
            ingestionService.getActiveMarketEvents()));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<Object>> getById(
            @PathVariable Long eventId) {
        log.info("GET /api/v1/market-events/{}", eventId);
        return ResponseEntity.ok(ApiResponse.success(
            "Market event fetched",
            ingestionService.getMarketEventById(eventId)));
    }

    @PostMapping("/mcp/simulate")
    public ResponseEntity<ApiResponse<Object>> simulateMcp(
            @Valid @RequestBody McpNewsItem item) {
        log.info("POST /mcp/simulate: {}", item.getHeadline());
        MarketEventIngestResponse resp = mcpNewsSimulatorService.simulateSingleIngest(item);
        HttpStatus status = "INGESTION_FAILED".equals(resp.getStatus())
            ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.CREATED;
        return ResponseEntity.status(status)
            .body(ApiResponse.success("MCP simulation complete", resp));
    }

    @PostMapping("/mcp/simulate-batch")
    public ResponseEntity<ApiResponse<Object>> simulateMcpBatch(
            @RequestBody List<McpNewsItem> items) {
        log.info("POST /mcp/simulate-batch: {} items", items.size());
        List<MarketEventIngestResponse> results =
            mcpNewsSimulatorService.simulateBatchIngest(items);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                "MCP batch: " + results.size() + "/" + items.size(), results));
    }

    @PostMapping("/live-news/trigger")
    public ResponseEntity<ApiResponse<Object>> triggerNewsIngest() {
        log.info("POST /live-news/trigger");
        newsApiMarketEventPollerImpl.pollAndIngest();
        return ResponseEntity.ok(ApiResponse.success("Live news ingestion triggered", null));
    }

    @PostMapping("/funds/sync-nav")
    public ResponseEntity<ApiResponse<Object>> triggerNavSync() {
        log.info("POST /funds/sync-nav");
        amfiFundSyncServiceImpl.syncFundNAV();
        return ResponseEntity.ok(ApiResponse.success("Fund NAV sync triggered", null));
    }
}
