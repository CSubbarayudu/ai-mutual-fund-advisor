package com.nihilent.mutualfund.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationResponseDto {
    private Long investorId;
    private Long fundId;
    private String fundName;
    private String explanation;
    private BigDecimal baseMatchScore;
    private BigDecimal marketAdjustedScore;
    private String effectiveRiskLevel;
    private LocalDateTime generatedAt;
}
