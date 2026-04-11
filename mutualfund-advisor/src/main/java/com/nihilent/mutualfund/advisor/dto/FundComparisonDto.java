package com.nihilent.mutualfund.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundComparisonDto {

    private FundSummaryDto fund1;
    private FundSummaryDto fund2;
    private String comparisonNote;
}
