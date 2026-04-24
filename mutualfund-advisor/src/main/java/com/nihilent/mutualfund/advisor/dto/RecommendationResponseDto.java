package com.nihilent.mutualfund.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponseDto {

    private Long recommendationId;
    private Long investorId;
    private Long fundId;
    private String fundName;
    private String category;
    private String fundRiskLevel;
    private Double baseMatchScore;
    private Double marketAdjustedScore;
    private LocalDateTime generatedAt;
}
