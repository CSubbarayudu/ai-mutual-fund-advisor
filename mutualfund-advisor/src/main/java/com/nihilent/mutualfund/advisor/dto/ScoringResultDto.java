package com.nihilent.mutualfund.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringResultDto {

    private Long investorId;
    private Long fundId;
    private String fundName;
    private BigDecimal baseMatchScore;
    private BigDecimal marketAdjustedScore;
    private BigDecimal confidenceScore;
    private String scoringSummary;
    private boolean alertGenerated;
}
