package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.request.DocumentIngestionRequest;

public interface DocumentIngestionService {
    int ingestDocument(DocumentIngestionRequest request);
}
