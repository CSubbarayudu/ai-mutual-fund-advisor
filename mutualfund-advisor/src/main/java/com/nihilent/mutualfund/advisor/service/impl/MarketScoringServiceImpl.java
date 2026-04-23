package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.ScoringResultDto;
import com.nihilent.mutualfund.advisor.entity.*;
import com.nihilent.mutualfund.advisor.exception.FundNotFoundException;
import com.nihilent.mutualfund.advisor.exception.InvestorNotFoundException;
import com.nihilent.mutualfund.advisor.exception.RiskAssessmentNotFoundException;
import com.nihilent.mutualfund.advisor.repository.*;
import com.nihilent.mutualfund.advisor.service.AlertGeneratorService;
import com.nihilent.mutualfund.advisor.service.MarketScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MarketScoringServiceImpl implements MarketScoringService {

    private final InvestorProfileRepository investorProfileRepository;
    private final MutualFundRepository mutualFundRepository;
    private final FundSectorAllocationRepository fundSectorAllocationRepository;
    private final MarketEventSectorRepository marketEventSectorRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserAlertRepository userAlertRepository;
    private final AlertGeneratorService alertGeneratorService;
    private final RiskAssessmentRepository riskAssessmentRepository;

    @Override
    public ScoringResultDto scoreOneFundForInvestor(Long investorId, Long fundId) {
        InvestorProfile investor = investorProfileRepository
                .findById(investorId)
                .orElseThrow(() -> new InvestorNotFoundException(investorId));
        MutualFund fund = mutualFundRepository
                .findById(fundId)
                .orElseThrow(() -> new FundNotFoundException(fundId));
        String investorRiskLevel = riskAssessmentRepository
                .findTopByInvestor_InvestorIdOrderByAssessedAtDesc(investorId)
                .map(RiskAssessment::getRiskLevel)
                .orElseThrow(() -> new RiskAssessmentNotFoundException(investorId));

        BigDecimal baseMatchScore = BigDecimal.ZERO;

        if (fund.getRiskLevel().equalsIgnoreCase(investorRiskLevel)) {
            baseMatchScore = baseMatchScore.add(BigDecimal.valueOf(40));
        } else {
            baseMatchScore = baseMatchScore.add(BigDecimal.valueOf(20));
        }

        if (fund.getReturn3y() != null) {
            if (fund.getReturn3y().compareTo(BigDecimal.valueOf(12.0)) >= 0) {
                baseMatchScore = baseMatchScore.add(BigDecimal.valueOf(30));
            } else if (fund.getReturn3y().compareTo(BigDecimal.valueOf(8.0)) >= 0) {
                baseMatchScore = baseMatchScore.add(BigDecimal.valueOf(20));
            } else {
                baseMatchScore = baseMatchScore.add(BigDecimal.valueOf(10));
            }
        }

        if (fund.getExpenseRatio() != null) {
            if (fund.getExpenseRatio().compareTo(BigDecimal.valueOf(1.0)) <= 0) {
                // no penalty
            } else if (fund.getExpenseRatio().compareTo(BigDecimal.valueOf(2.0)) <= 0) {
                baseMatchScore = baseMatchScore.subtract(BigDecimal.valueOf(5));
            } else {
                baseMatchScore = baseMatchScore.subtract(BigDecimal.valueOf(10));
            }
        }

        BigDecimal totalNegativeImpact = BigDecimal.ZERO;
        BigDecimal totalPositiveImpact = BigDecimal.ZERO;
        BigDecimal timeDecay = BigDecimal.ONE;
        Long mostImpactfulEventId = null;

        List<FundSectorAllocation> allocations = fundSectorAllocationRepository.findByFund_FundId(fundId);

        for (FundSectorAllocation allocation : allocations) {
            Long sectorId = allocation.getSector().getSectorId();
            List<MarketEventSector> eventSectors = marketEventSectorRepository.findBySector_SectorId(sectorId);

            for (MarketEventSector mes : eventSectors) {
                MarketEvent event = mes.getMarketEvent();

                boolean isActive =
                        (event.getExpiryDate() == null) || (event.getExpiryDate().isAfter(LocalDateTime.now()));
                if (!isActive) {
                    continue;
                }

                BigDecimal allocationFraction =
                        allocation.getAllocationPercentage().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

                BigDecimal credibility =
                        event.getCredibilityScore() != null ? event.getCredibilityScore() : BigDecimal.ONE;

                BigDecimal rawImpact =
                        allocationFraction
                                .multiply(mes.getImpactSeverity())
                                .multiply(credibility)
                                .multiply(timeDecay);

                if ("NEGATIVE".equalsIgnoreCase(event.getImpactType())) {
                    totalNegativeImpact = totalNegativeImpact.add(rawImpact);
                    mostImpactfulEventId = event.getEventId();
                } else if ("POSITIVE".equalsIgnoreCase(event.getImpactType())) {
                    totalPositiveImpact = totalPositiveImpact.add(rawImpact);
                }
            }
        }

        BigDecimal marketAdjustedScore =
                baseMatchScore.subtract(totalNegativeImpact).add(totalPositiveImpact);

        // ✅ FIX 1: Minimum score floor is 10, never allow 0
        // Reason: A score of 0 looks like a crash/error to investors and seniors
        // Even a heavily impacted fund should show a minimum signal of 10
        if (marketAdjustedScore.compareTo(BigDecimal.valueOf(10)) < 0) {
            marketAdjustedScore = BigDecimal.valueOf(10);
        }
        if (marketAdjustedScore.compareTo(BigDecimal.valueOf(100)) > 0) {
            marketAdjustedScore = BigDecimal.valueOf(100);
        }

        long activeEventCount =
                allocations.stream()
                        .flatMap(
                                a -> marketEventSectorRepository
                                        .findBySector_SectorId(a.getSector().getSectorId())
                                        .stream())
                        .map(MarketEventSector::getMarketEvent)
                        .filter(e -> e.getExpiryDate() == null || e.getExpiryDate().isAfter(LocalDateTime.now()))
                        .map(MarketEvent::getEventId)
                        .distinct()
                        .count();

        // ✅ Confidence logic is CORRECT as-is — no change needed
        // 0 events = 100 (no turbulence = high confidence)
        // 1 event  = 95  (some risk detected = slight uncertainty)
        // 2 events = 90, and so on — will vary naturally as more events are added
        BigDecimal confidenceScore = BigDecimal.valueOf(100 - (activeEventCount * 5));
        if (confidenceScore.compareTo(BigDecimal.ZERO) < 0) {
            confidenceScore = BigDecimal.ZERO;
        }

        Optional<Recommendation> existing =
                recommendationRepository.findByInvestor_InvestorIdAndFund_FundId(investorId, fundId);

        Recommendation rec;
        if (existing.isPresent()) {
            rec = existing.get();
            rec.setBaseMatchScore(baseMatchScore);
            rec.setMarketAdjustedScore(marketAdjustedScore);
            rec.setConfidenceScore(confidenceScore);
            rec.setGeneratedAt(LocalDateTime.now());
        } else {
            rec = new Recommendation();
            rec.setInvestor(investor);
            rec.setFund(fund);
            rec.setBaseMatchScore(baseMatchScore);
            rec.setMarketAdjustedScore(marketAdjustedScore);
            rec.setConfidenceScore(confidenceScore);
            rec.setRecommendationStatus("ACTIVE");
            rec.setModelVersion("v2-market-aware");
            rec.setGeneratedAt(LocalDateTime.now());
        }

        Map<String, Object> explanation = Map.of(
                "riskMatch", investorRiskLevel,
                "baseScore", baseMatchScore,
                "marketAdjustedScore", marketAdjustedScore,
                "confidenceScore", confidenceScore,
                "activeSectorImpacts", activeEventCount,
                "scoreDrop", baseMatchScore.subtract(marketAdjustedScore),
                "dataQuality", "SYSTEM_SCORED"
        );

        rec.setExplanationData(explanation);

        Recommendation savedRecommendation = recommendationRepository.save(rec);

        BigDecimal scoreDrop = baseMatchScore.subtract(marketAdjustedScore);
        boolean alertGenerated = false;

        if (scoreDrop.compareTo(BigDecimal.valueOf(10.0)) >= 0) {
            Long userId = investor.getUser().getUserId();
            alertGeneratorService.generateAlert(
                    userId, savedRecommendation.getRecommendationId(), mostImpactfulEventId, scoreDrop);
            alertGenerated = true;
        }

        String scoringSummary =
                "Fund: "
                        + fund.getFundName()
                        + " | Base: "
                        + baseMatchScore.setScale(1, RoundingMode.HALF_UP)
                        + " | Adjusted: "
                        + marketAdjustedScore.setScale(1, RoundingMode.HALF_UP)
                        + " | Confidence: "
                        + confidenceScore.setScale(0, RoundingMode.HALF_UP)
                        + "%";

        return ScoringResultDto.builder()
                .investorId(investorId)
                .fundId(fundId)
                .fundName(fund.getFundName())
                .baseMatchScore(baseMatchScore)
                .marketAdjustedScore(marketAdjustedScore)
                .confidenceScore(confidenceScore)
                .scoringSummary(scoringSummary)
                .alertGenerated(alertGenerated)
                .build();
    }

    @Override
    public List<ScoringResultDto> scoreAllFundsForInvestor(Long investorId) {
        List<MutualFund> activeFunds =
                mutualFundRepository.findAll().stream()
                        .filter(f -> f.getFundStatus() != null && "ACTIVE".equalsIgnoreCase(f.getFundStatus()))
                        .toList();

        List<ScoringResultDto> results = new ArrayList<>();
        for (MutualFund fund : activeFunds) {
            results.add(scoreOneFundForInvestor(investorId, fund.getFundId()));
        }

        log.info("Scoring complete for investorId={} — {} funds scored", investorId, results.size());
        return results;
    }

    @Override
    public void runFullMarketScoringCycle() {
        LocalDateTime started = LocalDateTime.now();
        log.info("Market scoring cycle started at {}", started);

        List<InvestorProfile> investors = investorProfileRepository.findAll();
        for (InvestorProfile investor : investors) {
            scoreAllFundsForInvestor(investor.getInvestorId());
        }

        log.info("Market scoring cycle completed. {} investors processed.", investors.size());
    }
}