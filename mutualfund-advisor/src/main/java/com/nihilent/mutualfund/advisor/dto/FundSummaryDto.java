package com.nihilent.mutualfund.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundSummaryDto {

    private Long fundId;
    private String fundName;
    private String amcName;
    private String category;
    private String riskLevel;
    private BigDecimal expenseRatio;
    private BigDecimal return1y;
    private BigDecimal return3y;
    private BigDecimal minimumInvestment;
    private String investmentHorizon;
    private String fundStatus;
    private BigDecimal volatilityScore;
}
