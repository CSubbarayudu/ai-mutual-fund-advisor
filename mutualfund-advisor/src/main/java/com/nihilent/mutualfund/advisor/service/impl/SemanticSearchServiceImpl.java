package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.response.RagSearchResultDto;
import com.nihilent.mutualfund.advisor.entity.FundDocumentChunk;
import com.nihilent.mutualfund.advisor.repository.FundDocumentChunkRepository;
import com.nihilent.mutualfund.advisor.service.SemanticSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticSearchServiceImpl implements SemanticSearchService {

    private final FundDocumentChunkRepository fundDocumentChunkRepository;

    private static final Set<String> STOPWORDS = Set.of(
            "what", "is", "the", "a", "an", "of", "for", "in", "on", "at", "to", "and", "or",
            "how", "why", "tell", "me", "about", "fund", "mutual", "india", "market",
            "should", "invest", "my", "this", "that", "with", "are", "does", "do", "has");

    private List<String> extractKeywords(String query) {
        return Arrays.stream(query.toLowerCase().split("\\s+"))
                .filter(w -> w.length() > 2 && !STOPWORDS.contains(w))
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
    }

    @Override
    public List<RagSearchResultDto> search(String query, int maxResults) {
        List<String> keywords = extractKeywords(query);
        if (keywords.isEmpty()) return Collections.emptyList();

        Map<Long, FundDocumentChunk> deduped = new LinkedHashMap<>();
        for (String kw : keywords) {
            fundDocumentChunkRepository.findByKeyword(kw)
                    .forEach(c -> deduped.putIfAbsent(c.getChunkId(), c));
        }

        return deduped.values().stream()
                .limit(maxResults)
                .map(c -> RagSearchResultDto.builder()
                        .chunkId(c.getChunkId())
                        .documentId(c.getDocument().getDocumentId())
                        .fundId(c.getDocument().getFund().getFundId())
                        .fundName(c.getDocument().getFund().getFundName())
                        .docType(c.getDocument().getDocType())
                        .chunkText(c.getChunkText())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public String buildRagContext(String query, int maxResults) {
        List<RagSearchResultDto> results = search(query, maxResults);
        if (results.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("\n--- RELEVANT FUND KNOWLEDGE ---\n");
        for (RagSearchResultDto r : results) {
            sb.append("Source: ").append(r.getFundName())
              .append(" | ").append(r.getDocType()).append("\n")
              .append(r.getChunkText()).append("\n---\n");
        }
        return sb.toString();
    }
}
