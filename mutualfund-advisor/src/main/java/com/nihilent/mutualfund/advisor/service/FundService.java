package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.FundComparisonDto;
import com.nihilent.mutualfund.advisor.dto.FundSummaryDto;

import java.util.List;

public interface FundService {

    List<FundSummaryDto> getAllActiveFunds();

    FundSummaryDto getFundById(Long fundId);

    FundComparisonDto compareFunds(Long fund1Id, Long fund2Id);
}
