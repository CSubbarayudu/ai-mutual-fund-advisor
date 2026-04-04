package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.ScoringResultDto;

import java.util.List;

public interface MarketScoringService {

    ScoringResultDto scoreOneFundForInvestor(Long investorId, Long fundId);

    List<ScoringResultDto> scoreAllFundsForInvestor(Long investorId);

    void runFullMarketScoringCycle();
}
