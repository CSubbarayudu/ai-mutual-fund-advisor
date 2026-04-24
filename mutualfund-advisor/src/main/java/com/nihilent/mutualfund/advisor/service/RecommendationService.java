package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.RecommendationResponseDto;
import com.nihilent.mutualfund.advisor.entity.Recommendation;

import java.util.List;

public interface RecommendationService {

    List<Recommendation> generateRecommendations(Long investorId);

    List<RecommendationResponseDto> getRecommendationsByInvestorId(Long investorId);
}
