package com.nihilent.mutualfund.advisor.service.impl;

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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final InvestorProfileRepository investorRepo;
    private final RiskAssessmentRepository assessmentRepo;
    private final MutualFundRepository fundRepo;
    private final RecommendationRepository recommendationRepo;

    @Override
    public List<Recommendation> generateRecommendations(Long investorId) {

        InvestorProfile investor = investorRepo.findById(investorId)
                .orElseThrow(() -> new RuntimeException("Investor not found"));

        // Get latest risk assessment
        RiskAssessment assessment = assessmentRepo
                .findTopByInvestorInvestorIdOrderByAssessedAtDesc(investorId)
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
            rec.setMatchScore(calculateScore(fund, investor));
            rec.setRecommendationReason("Matches your risk level: " + riskLevel);
            rec.setRecommendationStatus("ACTIVE");
            rec.setModelVersion("v1");
            rec.setGeneratedAt(LocalDateTime.now());

            recommendations.add(recommendationRepo.save(rec));
        }

        return recommendations;
    }

    private BigDecimal calculateScore(MutualFund fund, InvestorProfile investor) {
        return BigDecimal.valueOf(80 + Math.random() * 20); // simple scoring for now
    }
}
