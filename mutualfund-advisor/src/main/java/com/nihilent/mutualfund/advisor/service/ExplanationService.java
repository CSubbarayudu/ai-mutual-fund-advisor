package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.ExplanationResponseDto;

public interface ExplanationService {
    ExplanationResponseDto explainFundForInvestor(Long investorId, Long fundId);
}
