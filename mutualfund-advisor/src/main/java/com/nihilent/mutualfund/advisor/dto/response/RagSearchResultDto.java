package com.nihilent.mutualfund.advisor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagSearchResultDto {
    private Long chunkId;
    private Long documentId;
    private Long fundId;
    private String fundName;
    private String docType;
    private String chunkText;
}
