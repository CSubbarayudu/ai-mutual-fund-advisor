package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.ScoringResultDto;
import com.nihilent.mutualfund.advisor.dto.WhatIfRequestDto;
import com.nihilent.mutualfund.advisor.entity.FundSectorAllocation;
import com.nihilent.mutualfund.advisor.entity.MarketEvent;
import com.nihilent.mutualfund.advisor.entity.MarketEventSector;
import com.nihilent.mutualfund.advisor.entity.MutualFund;
import com.nihilent.mutualfund.advisor.entity.RiskAssessment;
import com.nihilent.mutualfund.advisor.exception.InvestorNotFoundException;
import com.nihilent.mutualfund.advisor.exception.RiskAssessmentNotFoundException;
import com.nihilent.mutualfund.advisor.repository.FundSectorAllocationRepository;
import com.nihilent.mutualfund.advisor.repository.InvestorProfileRepository;
import com.nihilent.mutualfund.advisor.repository.MarketEventSectorRepository;
import com.nihilent.mutualfund.advisor.repository.MutualFundRepository;
import com.nihilent.mutualfund.advisor.repository.RiskAssessmentRepository;
import com.nihilent.mutualfund.advisor.service.WhatIfSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WhatIfSimulationServiceImpl implements WhatIfSimulationService {

    private final InvestorProfileRepository investorProfileRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final MutualFundRepository mutualFundRepository;
    private final FundSectorAllocationRepository fundSectorAllocationRepository;
    private final MarketEventSectorRepository marketEventSectorRepository;

    @Override
    public List<ScoringResultDto> simulate(Long investorId, WhatIfRequestDto overrides) {
        log.info("Running what-if simulation for investorId={} overrides={}", investorId, overrides);

        investorProfileRepository.findById(investorId)
                .orElseThrow(() -> new InvestorNotFoundException(investorId));

        String assessedRiskLevel = riskAssessmentRepository
                .findTopByInvestor_InvestorIdOrderByAssessedAtDesc(investorId)
                .map(RiskAssessment::getRiskLevel)
                .orElseThrow(() -> new RiskAssessmentNotFoundException(investorId));

        String effectiveRiskLevel =
                (overrides.getRiskLevel() != null && !overrides.getRiskLevel().isBlank())
                        ? overrides.getRiskLevel().toUpperCase()
                        : assessedRiskLevel;

        log.info("Simulation effectiveRiskLevel={} for investorId={}", effectiveRiskLevel, investorId);

        List<MutualFund> activeFunds = mutualFundRepository.findAll().stream()
                .filter(f -> "ACTIVE".equalsIgnoreCase(f.getFundStatus()))
                .toList();

        List<ScoringResultDto> results = new ArrayList<>();

        for (MutualFund fund : activeFunds) {
            results.add(scoreSimulated(investorId, fund, effectiveRiskLevel));
        }

        log.info("Simulation complete for investorId={} — {} funds evaluated", investorId, results.size());
        return results;
    }

    private ScoringResultDto scoreSimulated(Long investorId, MutualFund fund, String effectiveRiskLevel) {
        BigDecimal baseMatchScore = BigDecimal.ZERO;

        if (fund.getRiskLevel().equalsIgnoreCase(effectiveRiskLevel)) {
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
        LocalDateTime now = LocalDateTime.now();

        List<FundSectorAllocation> allocations =
                fundSectorAllocationRepository.findByFund_FundId(fund.getFundId());

        for (FundSectorAllocation allocation : allocations) {
            Long sectorId = allocation.getSector().getSectorId();
            List<MarketEventSector> eventSectors =
                    marketEventSectorRepository.findBySector_SectorId(sectorId);

            for (MarketEventSector mes : eventSectors) {
                MarketEvent event = mes.getMarketEvent();
                boolean isActive = (event.getExpiryDate() == null) || event.getExpiryDate().isAfter(now);
                if (!isActive) continue;

                BigDecimal allocationFraction = allocation.getAllocationPercentage()
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal credibility = event.getCredibilityScore() != null
                        ? event.getCredibilityScore()
                        : BigDecimal.ONE;

                BigDecimal rawImpact = allocationFraction
                        .multiply(mes.getImpactSeverity())
                        .multiply(credibility)
                        .multiply(timeDecay);

                if ("NEGATIVE".equalsIgnoreCase(event.getImpactType())) {
                    totalNegativeImpact = totalNegativeImpact.add(rawImpact);
                } else if ("POSITIVE".equalsIgnoreCase(event.getImpactType())) {
                    totalPositiveImpact = totalPositiveImpact.add(rawImpact);
                }
            }
        }

        BigDecimal marketAdjustedScore = baseMatchScore
                .subtract(totalNegativeImpact)
                .add(totalPositiveImpact);

        // ✅ FIX: Minimum floor 10 — matches MarketScoringServiceImpl
        if (marketAdjustedScore.compareTo(BigDecimal.valueOf(10)) < 0) {
            marketAdjustedScore = BigDecimal.valueOf(10);
        }
        if (marketAdjustedScore.compareTo(BigDecimal.valueOf(100)) > 0) {
            marketAdjustedScore = BigDecimal.valueOf(100);
        }

        long activeEventCount = allocations.stream()
                .flatMap(a -> marketEventSectorRepository
                        .findBySector_SectorId(a.getSector().getSectorId()).stream())
                .map(MarketEventSector::getMarketEvent)
                .filter(e -> e.getExpiryDate() == null || e.getExpiryDate().isAfter(now))
                .map(MarketEvent::getEventId)
                .distinct()
                .count();

        BigDecimal confidenceScore = BigDecimal.valueOf(100 - (activeEventCount * 5));
        if (confidenceScore.compareTo(BigDecimal.ZERO) < 0) {
            confidenceScore = BigDecimal.ZERO;
        }

        String scoringSummary = "[SIMULATION] Fund: " + fund.getFundName()
                + " | Base: " + baseMatchScore.setScale(1, RoundingMode.HALF_UP)
                + " | Adjusted: " + marketAdjustedScore.setScale(1, RoundingMode.HALF_UP)
                + " | Confidence: " + confidenceScore.setScale(0, RoundingMode.HALF_UP) + "%"
                + " | EffectiveRisk: " + effectiveRiskLevel;

        return ScoringResultDto.builder()
                .investorId(investorId)
                .fundId(fund.getFundId())
                .fundName(fund.getFundName())
                .baseMatchScore(baseMatchScore)
                .marketAdjustedScore(marketAdjustedScore)
                .confidenceScore(confidenceScore)
                .scoringSummary(scoringSummary)
                .alertGenerated(false)
                .build();
    }
}