package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.ExplanationResponseDto;
import com.nihilent.mutualfund.advisor.entity.*;
import com.nihilent.mutualfund.advisor.exception.*;
import com.nihilent.mutualfund.advisor.ai.FundAdvisorAi;
import com.nihilent.mutualfund.advisor.repository.*;
import com.nihilent.mutualfund.advisor.service.ExplanationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExplanationServiceImpl implements ExplanationService {

    private final InvestorProfileRepository investorProfileRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final MutualFundRepository mutualFundRepository;
    private final RecommendationRepository recommendationRepository;
    private final FundSectorAllocationRepository fundSectorAllocationRepository;
    private final MarketEventSectorRepository marketEventSectorRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final FundAdvisorAi fundAdvisorAi;

    @Override
    @Transactional
    public ExplanationResponseDto explainFundForInvestor(Long investorId, Long fundId) {
        log.info("Generating AI explanation for investorId={} fundId={}", investorId, fundId);

        // Step 1: Validate investor
        InvestorProfile investor = investorProfileRepository.findById(investorId)
                .orElseThrow(() -> new InvestorNotFoundException(investorId));

        // Step 2: Fetch risk level
        String riskLevel = riskAssessmentRepository
                .findTopByInvestor_InvestorIdOrderByAssessedAtDesc(investorId)
                .map(RiskAssessment::getRiskLevel)
                .orElseThrow(() -> new RiskAssessmentNotFoundException(investorId));

        // Step 3: Validate fund
        MutualFund fund = mutualFundRepository.findById(fundId)
                .orElseThrow(() -> new FundNotFoundException(fundId));

        // Step 4: Fetch recommendation (Optional — correct return type)
        Recommendation recommendation = recommendationRepository
                .findByInvestor_InvestorIdAndFund_FundId(investorId, fundId)
                .orElseThrow(() -> new RuntimeException(
                        "No scoring found for investor " + investorId +
                        " and fund " + fundId +
                        ". Please run POST /api/v1/scoring/trigger-cycle first."));

        // Step 5: Fetch sector allocations
        List<FundSectorAllocation> allocations =
                fundSectorAllocationRepository.findByFund_FundId(fundId);

        // Step 6: Build context string
        LocalDateTime now = LocalDateTime.now();
        StringBuilder ctx = new StringBuilder();
        ctx.append("Investor Risk Level: ").append(riskLevel).append("\n");
        ctx.append("Fund: ").append(fund.getFundName())
           .append(" | Category: ").append(fund.getCategory())
           .append(" | Fund Risk Level: ").append(fund.getRiskLevel()).append("\n");
        ctx.append("Expense Ratio: ").append(fund.getExpenseRatio())
           .append("% | 3-Year Return: ").append(fund.getReturn3y()).append("%\n");
        ctx.append("Base Match Score: ").append(recommendation.getBaseMatchScore())
           .append("/100 | Market Adjusted Score: ")
           .append(recommendation.getMarketAdjustedScore()).append("/100\n\n");
        ctx.append("Active Market Events Affecting This Fund:\n");

        boolean hasActiveEvents = false;
        for (FundSectorAllocation allocation : allocations) {
            Long sectorId = allocation.getSector().getSectorId();
            List<MarketEventSector> eventSectors =
                    marketEventSectorRepository.findBySector_SectorId(sectorId);

            for (MarketEventSector mes : eventSectors) {
                MarketEvent event = mes.getMarketEvent();
                boolean active = event.getExpiryDate() == null
                        || event.getExpiryDate().isAfter(now);
                if (!active) continue;

                hasActiveEvents = true;
                ctx.append("- Sector: ").append(allocation.getSector().getSectorName())
                   .append(" | Fund Allocation: ")
                   .append(allocation.getAllocationPercentage()).append("%\n");
                ctx.append("  * Event: ").append(event.getEventTitle())
                   .append(" | Type: ").append(event.getImpactType())
                   .append(" | Severity: ").append(mes.getImpactSeverity()).append("/10\n");
            }
        }

        if (!hasActiveEvents) {
            ctx.append("No active market events currently affecting this fund.\n");
        }

        ctx.append("\nQuestion: Why did this fund receive a market adjusted score of ")
           .append(recommendation.getMarketAdjustedScore()).append("/100?");

        log.debug("AI context built for fund={}: {}", fund.getFundName(), ctx);

        // Step 7: Call AI
        String explanation = fundAdvisorAi.explainFundScore(ctx.toString());
        log.info("AI explanation generated for fundId={}", fundId);

        // Step 8: Save to chat_history
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUser(investor.getUser());
        chatHistory.setInvestor(investor);
        chatHistory.setQuestion("Explain fund score: " + fund.getFundName()
                + " for investor " + investorId);
        chatHistory.setAnswer(explanation);
        chatHistory.setResponseType("EXPLANATION");
        chatHistory.setModelName("gpt-4o-mini");
        chatHistory.setAskedAt(now);
        chatHistoryRepository.save(chatHistory);

        // Step 9: Return DTO
        return ExplanationResponseDto.builder()
                .investorId(investorId)
                .fundId(fundId)
                .fundName(fund.getFundName())
                .explanation(explanation)
                .baseMatchScore(recommendation.getBaseMatchScore())
                .marketAdjustedScore(recommendation.getMarketAdjustedScore())
                .effectiveRiskLevel(riskLevel)
                .generatedAt(now)
                .build();
    }
}
