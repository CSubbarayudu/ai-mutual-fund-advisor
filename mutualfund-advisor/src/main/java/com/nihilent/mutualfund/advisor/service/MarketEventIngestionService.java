package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.request.MarketEventIngestRequest;
import com.nihilent.mutualfund.advisor.dto.response.MarketEventIngestResponse;
import java.util.List;

public interface MarketEventIngestionService {
    MarketEventIngestResponse ingestMarketEvent(MarketEventIngestRequest request);
    List<MarketEventIngestResponse> getActiveMarketEvents();
    MarketEventIngestResponse getMarketEventById(Long eventId);
}
