package com.nihilent.mutualfund.advisor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentIngestionRequest {
    @NotNull(message = "fundId is required") private Long fundId;
    @NotBlank(message = "docType is required") private String docType;
    @NotBlank(message = "fullContent is required") private String fullContent;
    private String contentSummary;
}
