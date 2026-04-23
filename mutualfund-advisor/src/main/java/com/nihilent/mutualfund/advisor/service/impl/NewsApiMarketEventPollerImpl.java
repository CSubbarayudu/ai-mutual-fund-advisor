package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.request.McpNewsItem;
import com.nihilent.mutualfund.advisor.dto.request.SectorImpactDto;
import com.nihilent.mutualfund.advisor.service.McpNewsSimulatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsApiMarketEventPollerImpl {

    private final McpNewsSimulatorService mcpNewsSimulatorService;
    private final RestTemplate restTemplate;

    @Value("${newsapi.key}")
    private String newsApiKey;

    @Value("${newsapi.enabled}")
    private boolean newsApiEnabled;

    @Value("${newsapi.base-url}")
    private String newsApiBaseUrl;

    private static final Map<String, String> KEYWORD_SECTOR_MAP = Map.of(
        "banking", "BANKING", "nifty bank", "BANKING",
        "pharma", "PHARMA",   "drug", "PHARMA",
        "technology", "IT",   "chip", "IT",
        "oil", "ENERGY",      "crude", "ENERGY",
        "realty", "REALESTATE", "metal", "METALS"
    );

    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void pollAndIngest() {
        if (!newsApiEnabled) return;
        log.info("NewsAPI polling started");
        try {
            String url = newsApiBaseUrl +
                "?q=india+mutual+fund+market+sector" +
                "&language=en&pageSize=5&apiKey=" + newsApiKey;
            Map resp = restTemplate.getForObject(url, Map.class);
            List<Map> articles = (List<Map>) resp.get("articles");
            for (Map article : articles) {
                String headline = (String) article.get("title");
                String sourceUrl = (String) article.get("url");
                McpNewsItem item = mapHeadlineToMcpItem(headline, sourceUrl);
                if (item != null) {
                    mcpNewsSimulatorService.simulateSingleIngest(item);
                    log.info("Live news ingested: {}", headline);
                }
            }
        } catch (Exception e) {
            log.error("NewsAPI polling failed: {}", e.getMessage());
        }
    }

    private McpNewsItem mapHeadlineToMcpItem(String headline, String sourceUrl) {
        if (headline == null) return null;
        String lower = headline.toLowerCase();
        for (Map.Entry<String, String> entry : KEYWORD_SECTOR_MAP.entrySet()) {
            if (lower.contains(entry.getKey())) {
                McpNewsItem item = new McpNewsItem();
                item.setHeadline(headline);
                item.setSource(sourceUrl);
                item.setImpactType(detectSentiment(lower));
                item.setCredibilityScore(BigDecimal.valueOf(0.75));
                item.setImpactDuration("30 days");
                item.setEventDate(LocalDateTime.now());
                item.setExpiryDate(LocalDateTime.now().plusDays(30));
                SectorImpactDto sectorDto = new SectorImpactDto();
                sectorDto.setSectorName(entry.getValue());
                sectorDto.setImpactSeverity(BigDecimal.valueOf(6.0));
                item.setAffectedSectors(List.of(sectorDto));
                return item;
            }
        }
        return null;
    }

    private String detectSentiment(String text) {
        if (text.contains("crash") || text.contains("fall") ||
            text.contains("ban") || text.contains("loss")) return "NEGATIVE";
        if (text.contains("growth") || text.contains("boost") ||
            text.contains("rise") || text.contains("approval")) return "POSITIVE";
        return "NEUTRAL";
    }
}
