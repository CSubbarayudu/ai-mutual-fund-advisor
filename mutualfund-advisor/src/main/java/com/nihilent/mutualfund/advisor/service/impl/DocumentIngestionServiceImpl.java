package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.request.DocumentIngestionRequest;
import com.nihilent.mutualfund.advisor.entity.FundDocument;
import com.nihilent.mutualfund.advisor.entity.FundDocumentChunk;
import com.nihilent.mutualfund.advisor.entity.MutualFund;
import com.nihilent.mutualfund.advisor.exception.FundNotFoundException;
import com.nihilent.mutualfund.advisor.repository.FundDocumentChunkRepository;
import com.nihilent.mutualfund.advisor.repository.FundDocumentRepository;
import com.nihilent.mutualfund.advisor.repository.MutualFundRepository;
import com.nihilent.mutualfund.advisor.service.DocumentIngestionService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentIngestionServiceImpl implements DocumentIngestionService {

    private final MutualFundRepository mutualFundRepository;
    private final FundDocumentRepository fundDocumentRepository;
    private final FundDocumentChunkRepository fundDocumentChunkRepository;
    private final EmbeddingModel embeddingModel;

    @Override
    public int ingestDocument(DocumentIngestionRequest request) {

        MutualFund fund = mutualFundRepository.findById(request.getFundId())
                .orElseThrow(() -> new FundNotFoundException(request.getFundId()));

        String content = request.getFullContent();

        String summary = request.getContentSummary() != null
                ? request.getContentSummary()
                : content.substring(0, Math.min(200, content.length()));

        // Save document
        FundDocument doc = new FundDocument();
        doc.setFund(fund);
        doc.setDocType(request.getDocType());
        doc.setContentSummary(summary);
        FundDocument savedDoc = fundDocumentRepository.save(doc);

        List<FundDocumentChunk> chunks = new ArrayList<>();

        int start = 0;

        while (start < content.length()) {

            int end = Math.min(start + 500, content.length());

            FundDocumentChunk chunk = new FundDocumentChunk();
            chunk.setDocument(savedDoc);

            String chunkText = content.substring(start, end);
            chunk.setChunkText(chunkText);

            log.debug("Generating embedding for chunk, fundId={}, documentId={}", fund.getFundId(), savedDoc.getDocumentId());

            Response<Embedding> emb = embeddingModel.embed(chunkText);
            float[] vec = emb.content().vector();

            log.debug("Embedding generated, size={}", vec.length);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < vec.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(vec[i]);
            }
            String vectorStr = sb.toString();

            chunk.setEmbeddingVector(vectorStr);

            chunks.add(chunk);

            start += 400; // overlap
        }

        fundDocumentChunkRepository.saveAll(chunks);

        log.info("🚀 Ingest completed fundId={} chunks={}", fund.getFundId(), chunks.size());

        return chunks.size();
    }
}