package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.response.RagSearchResultDto;
import java.util.List;

public interface SemanticSearchService {
    List<RagSearchResultDto> search(String query, int maxResults);
    String buildRagContext(String query, int maxResults);
}
