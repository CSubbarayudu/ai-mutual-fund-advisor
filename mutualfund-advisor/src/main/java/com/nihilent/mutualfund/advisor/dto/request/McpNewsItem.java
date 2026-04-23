package com.nihilent.mutualfund.advisor.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class McpNewsItem {
    @NotBlank(message = "Headline is required")
    private String headline;
    private String source;
    private String impactType;
    private BigDecimal credibilityScore;
    private String impactDuration;
    private LocalDateTime eventDate;
    private LocalDateTime expiryDate;
    @Valid
    private List<SectorImpactDto> affectedSectors;
}
