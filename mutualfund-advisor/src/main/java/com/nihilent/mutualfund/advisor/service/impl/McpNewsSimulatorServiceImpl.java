package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.request.MarketEventIngestRequest;
import com.nihilent.mutualfund.advisor.dto.request.McpNewsItem;
import com.nihilent.mutualfund.advisor.dto.response.MarketEventIngestResponse;
import com.nihilent.mutualfund.advisor.service.MarketEventIngestionService;
import com.nihilent.mutualfund.advisor.service.McpNewsSimulatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service @RequiredArgsConstructor @Slf4j
public class McpNewsSimulatorServiceImpl implements McpNewsSimulatorService {

    private final MarketEventIngestionService ingestionService;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 500L;

    @Override
    public List<MarketEventIngestResponse> simulateBatchIngest(List<McpNewsItem> items) {
        log.info("MCP batch: {} items", items.size());
        List<MarketEventIngestResponse> results = new ArrayList<>();
        for (McpNewsItem item : items) {
            try {
                results.add(simulateSingleIngest(item));
            } catch (Exception e) {
                log.error("MCP batch item skipped: {}", item.getHeadline());
            }
        }
        log.info("MCP batch done: {}/{}", results.size(), items.size());
        return results;
    }

    @Override
    public MarketEventIngestResponse simulateSingleIngest(McpNewsItem item) {
        MarketEventIngestRequest req = new MarketEventIngestRequest();
        req.setEventTitle(item.getHeadline());
        req.setEventDescription("MCP_SIMULATED: " + item.getHeadline());
        req.setImpactType(item.getImpactType() != null ? item.getImpactType() : "NEUTRAL");
        req.setCredibilityScore(item.getCredibilityScore() != null
            ? item.getCredibilityScore() : BigDecimal.valueOf(0.7));
        req.setImpactDuration(item.getImpactDuration() != null
            ? item.getImpactDuration() : "30 days");
        req.setSourceUrl(item.getSource() != null ? item.getSource() : "MCP_SIMULATED");
        req.setEventDate(item.getEventDate() != null
            ? item.getEventDate() : LocalDateTime.now());
        req.setExpiryDate(item.getExpiryDate() != null
            ? item.getExpiryDate() : LocalDateTime.now().plusDays(30));
        req.setAffectedSectors(item.getAffectedSectors() != null
            ? item.getAffectedSectors() : List.of());

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.info("MCP attempt {}/{}: {}", attempt, MAX_RETRIES, item.getHeadline());
                MarketEventIngestResponse resp = ingestionService.ingestMarketEvent(req);
                log.info("MCP SUCCESS attempt {}: eventId={}", attempt, resp.getEventId());
                return resp;
            } catch (Exception e) {
                log.warn("MCP attempt {} failed: {}", attempt, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(RETRY_DELAY_MS * attempt); }
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        }
        log.error("MCP FAILED after {} attempts: {}", MAX_RETRIES, item.getHeadline());
        return MarketEventIngestResponse.builder()
            .eventId(-1L)
            .eventTitle(item.getHeadline())
            .impactType(item.getImpactType())
            .affectedSectorsCount(0)
            .affectedFundsCount(0)
            .alertsGeneratedCount(0)
            .status("INGESTION_FAILED")
            .ingestedAt(LocalDateTime.now())
            .build();
    }
}
