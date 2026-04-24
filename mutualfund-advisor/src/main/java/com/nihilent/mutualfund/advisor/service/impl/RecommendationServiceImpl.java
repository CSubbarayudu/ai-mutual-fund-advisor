package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.RecommendationResponseDto;
import com.nihilent.mutualfund.advisor.entity.InvestorProfile;
import com.nihilent.mutualfund.advisor.entity.MutualFund;
import com.nihilent.mutualfund.advisor.entity.Recommendation;
import com.nihilent.mutualfund.advisor.entity.RiskAssessment;
import com.nihilent.mutualfund.advisor.repository.InvestorProfileRepository;
import com.nihilent.mutualfund.advisor.repository.MutualFundRepository;
import com.nihilent.mutualfund.advisor.repository.RecommendationRepository;
import com.nihilent.mutualfund.advisor.repository.RiskAssessmentRepository;
import com.nihilent.mutualfund.advisor.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final InvestorProfileRepository investorRepo;
    private final RiskAssessmentRepository assessmentRepo;
    private final MutualFundRepository fundRepo;
    private final RecommendationRepository recommendationRepo;

    @Override
    @Transactional
    public List<Recommendation> generateRecommendations(Long investorId) {

        InvestorProfile investor = investorRepo.findById(investorId)
                .orElseThrow(() -> new RuntimeException("Investor not found"));

        // Get latest risk assessment
        RiskAssessment assessment = assessmentRepo
                .findTopByInvestor_InvestorIdOrderByAssessedAtDesc(investorId)
                .orElseThrow(() -> new RuntimeException("No assessment found"));

        String riskLevel = assessment.getRiskLevel();
        System.out.println("Investor Risk Level: " + riskLevel);

        // Get matching funds
        List<MutualFund> allFunds = fundRepo.findAll();
        System.out.println("Total Funds in DB: " + allFunds.size());

        List<MutualFund> funds = allFunds.stream()
                .filter(f -> f.getRiskLevel() != null &&
                        f.getRiskLevel().trim().equalsIgnoreCase(riskLevel.trim()))
                .toList();
        System.out.println("Matched Funds: " + funds.size());

        if (funds.isEmpty()) {
            throw new RuntimeException("No matching funds found for risk level: " + riskLevel);
        }

        List<Recommendation> recommendations = new ArrayList<>();

        for (MutualFund fund : funds) {

            Recommendation rec = new Recommendation();
            rec.setInvestor(investor);
            rec.setFund(fund);
            BigDecimal baseScore = calculateScore(fund, investor);
            rec.setBaseMatchScore(baseScore);
            rec.setMarketAdjustedScore(baseScore);
            rec.setConfidenceScore(new BigDecimal("100.00"));
            rec.setRecommendationReason("Matches your risk level: " + riskLevel);
            rec.setRecommendationStatus("ACTIVE");
            rec.setModelVersion("v1");
            rec.setGeneratedAt(LocalDateTime.now());

            recommendations.add(recommendationRepo.save(rec));
        }

        return recommendations;
    }

    @Override
    public List<RecommendationResponseDto> getRecommendationsByInvestorId(Long investorId) {

        // Step A — Fetch all recommendations with JOIN FETCH
        List<Recommendation> all = recommendationRepo
                .findByInvestor_InvestorIdOrderByMarketAdjustedScoreDesc(investorId);

        if (all == null || all.isEmpty()) {
            log.warn("No recommendations found for investorId: {}", investorId);
            return Collections.emptyList();
        }

        // Step B — Deduplicate in-memory by fundId (keep LATEST generatedAt only)
        Map<Long, Recommendation> latestByFund = new LinkedHashMap<>();
        for (Recommendation rec : all) {
            Long fid = rec.getFund().getFundId();
            if (!latestByFund.containsKey(fid) ||
                    rec.getGeneratedAt().isAfter(latestByFund.get(fid).getGeneratedAt())) {
                latestByFund.put(fid, rec);
            }
        }

        // Step C — Sort deduplicated results by marketAdjustedScore descending
        List<Recommendation> deduped = new ArrayList<>(latestByFund.values());
        deduped.sort(Comparator.comparingDouble(
                (Recommendation r) -> r.getMarketAdjustedScore().doubleValue()).reversed());

        // Step D — Map each entity to DTO
        return deduped.stream()
                .map(rec -> RecommendationResponseDto.builder()
                        .recommendationId(rec.getRecommendationId())
                        .investorId(rec.getInvestor().getInvestorId())
                        .fundId(rec.getFund().getFundId())
                        .fundName(rec.getFund().getFundName())
                        .category(rec.getFund().getCategory())
                        .fundRiskLevel(rec.getFund().getRiskLevel())
                        .baseMatchScore(rec.getBaseMatchScore() != null
                                ? rec.getBaseMatchScore().doubleValue() : 0.0)
                        .marketAdjustedScore(rec.getMarketAdjustedScore() != null
                                ? rec.getMarketAdjustedScore().doubleValue() : 0.0)
                        .generatedAt(rec.getGeneratedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private BigDecimal calculateScore(MutualFund fund, InvestorProfile investor) {
        return BigDecimal.valueOf(80 + Math.random() * 20); // simple scoring for now
    }
}
