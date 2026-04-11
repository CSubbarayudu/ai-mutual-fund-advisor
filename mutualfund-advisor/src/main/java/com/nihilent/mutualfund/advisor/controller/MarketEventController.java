package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.ApiResponse;
import com.nihilent.mutualfund.advisor.dto.request.MarketEventIngestRequest;
import com.nihilent.mutualfund.advisor.service.MarketEventIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/market-events")
@RequiredArgsConstructor
@Slf4j
public class MarketEventController {

    private final MarketEventIngestionService ingestionService;

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
}
