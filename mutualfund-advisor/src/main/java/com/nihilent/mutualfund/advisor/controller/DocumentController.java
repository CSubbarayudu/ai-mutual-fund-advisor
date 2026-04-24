package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.ApiResponse;
import com.nihilent.mutualfund.advisor.dto.request.DocumentIngestionRequest;
import com.nihilent.mutualfund.advisor.service.DocumentIngestionService;
import com.nihilent.mutualfund.advisor.service.SemanticSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentIngestionService documentIngestionService;
    private final SemanticSearchService semanticSearchService;

    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<Object>> ingest(@Valid @RequestBody DocumentIngestionRequest request) {
        int count = documentIngestionService.ingestDocument(request);
        return ResponseEntity.ok(ApiResponse.success(count + " chunks ingested for fundId=" + request.getFundId(), null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Object>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int maxResults) {
        return ResponseEntity.ok(ApiResponse.success("RAG search results",
                semanticSearchService.search(query, maxResults)));
    }
}
