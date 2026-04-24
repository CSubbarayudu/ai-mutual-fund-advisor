package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.response.RagSearchResultDto;
import com.nihilent.mutualfund.advisor.entity.FundDocumentChunk;
import com.nihilent.mutualfund.advisor.repository.FundDocumentChunkRepository;
import com.nihilent.mutualfund.advisor.service.SemanticSearchService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
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
    private final EmbeddingModel embeddingModel;

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
        log.info("RAG search: '{}'", query);
        try {
            Response<Embedding> resp = embeddingModel.embed(query);
            float[] qVec = resp.content().vector();
            List<FundDocumentChunk> all = fundDocumentChunkRepository.findAll();
            List<FundDocumentChunk> withVec = all.stream()
                .filter(c -> c.getEmbeddingVector() != null
                    && !c.getEmbeddingVector().isBlank())
                .collect(Collectors.toList());
            if (!withVec.isEmpty()) {
                List<RagSearchResultDto> ranked = withVec.stream()
                    .map(c -> {
                        float[] parsed = parseVector(c.getEmbeddingVector(), c.getChunkId());
                        return parsed != null ? Map.entry(c, cosineSimilarity(qVec, parsed)) : null;
                    })
                    .filter(Objects::nonNull)
                    .sorted(Map.Entry.<FundDocumentChunk, Double>
                        comparingByValue().reversed())
                    .limit(maxResults)
                    .map(e -> toDto(e.getKey()))
                    .collect(Collectors.toList());
                log.info("Vector RAG: {} chunks ranked", ranked.size());
                return ranked;
            }
        } catch (Exception e) {
            log.warn("Vector search failed, using keyword fallback: {}", e.getMessage());
        }
        log.info("Keyword RAG fallback for: '{}'", query);
        List<String> kws = extractKeywords(query);
        if (kws.isEmpty()) return List.of();
        Map<Long, FundDocumentChunk> deduped = new LinkedHashMap<>();
        for (String kw : kws)
            fundDocumentChunkRepository.findByKeyword(kw)
                .forEach(c -> deduped.putIfAbsent(c.getChunkId(), c));
        return deduped.values().stream()
            .limit(maxResults).map(this::toDto).collect(Collectors.toList());
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

    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) return 0.0;
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i];
        }
        double denom = Math.sqrt(na) * Math.sqrt(nb);
        return denom == 0 ? 0.0 : dot / denom;
    }

    private float[] parseVector(String s, Long chunkId) {
        if (s == null || s.isBlank()) {
            log.warn("Skipping null/empty vector string for chunkId={}", chunkId);
            return null;
        }
        s = s.replaceAll("[\\[\\]]", "").trim();
        try {
            String[] parts = s.split(",");
            float[] v = new float[parts.length];
            for (int i = 0; i < parts.length; i++)
                v[i] = Float.parseFloat(parts[i].trim());
            return v;
        } catch (NumberFormatException e) {
            log.warn("Unparseable vector entry, skipping. chunkId={}, error={}", chunkId, e.getMessage());
            return null;
        }
    }

    private RagSearchResultDto toDto(FundDocumentChunk c) {
        return RagSearchResultDto.builder()
            .chunkId(c.getChunkId())
            .documentId(c.getDocument().getDocumentId())
            .fundId(c.getDocument().getFund().getFundId())
            .fundName(c.getDocument().getFund().getFundName())
            .docType(c.getDocument().getDocType())
            .chunkText(c.getChunkText())
            .build();
    }
}
