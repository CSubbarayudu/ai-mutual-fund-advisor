package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.request.McpNewsItem;
import com.nihilent.mutualfund.advisor.dto.response.MarketEventIngestResponse;
import java.util.List;

public interface McpNewsSimulatorService {
    MarketEventIngestResponse simulateSingleIngest(McpNewsItem item);
    List<MarketEventIngestResponse> simulateBatchIngest(List<McpNewsItem> items);
}
