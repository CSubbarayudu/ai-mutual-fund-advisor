package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.ai.InvestorChatbotAi;
import com.nihilent.mutualfund.advisor.dto.ChatRequestDto;
import com.nihilent.mutualfund.advisor.dto.ChatResponseDto;
import com.nihilent.mutualfund.advisor.entity.*;
import com.nihilent.mutualfund.advisor.exception.*;
import com.nihilent.mutualfund.advisor.repository.*;
import com.nihilent.mutualfund.advisor.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatbotServiceImpl implements ChatbotService {

    private final InvestorProfileRepository investorProfileRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserAlertRepository userAlertRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final InvestorChatbotAi investorChatbotAi;
    private final com.nihilent.mutualfund.advisor.service.SemanticSearchService semanticSearchService;
    private final MarketEventRepository marketEventRepository;
    private final MarketEventSectorRepository marketEventSectorRepository;
    private final FundSectorAllocationRepository fundSectorAllocationRepository;

    @Override
    @Transactional
    public ChatResponseDto chat(Long investorId, ChatRequestDto request) {

        InvestorProfile investor = investorProfileRepository.findById(investorId)
                .orElseThrow(() -> new InvestorNotFoundException(investorId));

        Long userId = investor.getUser().getUserId();

        String riskLevel = riskAssessmentRepository
                .findTopByInvestor_InvestorIdOrderByAssessedAtDesc(investorId)
                .map(RiskAssessment::getRiskLevel)
                .orElse("NOT ASSESSED");

        List<Recommendation> topRecs = recommendationRepository
                .findByInvestor_InvestorIdOrderByMarketAdjustedScoreDesc(investorId)
                .stream()
                .limit(3)
                .collect(Collectors.toList());

        List<Long> investorSectorIds = topRecs.stream()
                .flatMap(rec -> fundSectorAllocationRepository
                        .findByFund_FundId(rec.getFund().getFundId()).stream())
                .map(alloc -> alloc.getSector().getSectorId())
                .distinct()
                .collect(Collectors.toList());

        List<MarketEvent> relevantEvents = marketEventRepository
                .findByExpiryDateAfter(LocalDateTime.now()).stream()
                .filter(ev -> marketEventSectorRepository
                        .findByMarketEventEventId(ev.getEventId()).stream()
                        .anyMatch(mes -> investorSectorIds.contains(
                                mes.getSector().getSectorId())))
                .limit(3)
                .collect(Collectors.toList());

        List<UserAlert> alerts = userAlertRepository
                .findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .limit(3)
                .collect(Collectors.toList());

        List<ChatHistory> recentHistory = chatHistoryRepository
                .findByInvestor_InvestorIdOrderByAskedAtDesc(investorId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        StringBuilder ctx = new StringBuilder();

        ctx.append("=== INVESTOR PROFILE ===\n");
        ctx.append("Name: ").append(investor.getUser().getFullName()).append("\n");
        ctx.append("Risk Level: ").append(riskLevel).append("\n\n");

        ctx.append("=== TOP FUND RECOMMENDATIONS ===\n");

        if (topRecs.isEmpty()) {
            ctx.append("No recommendations available.\n");
        } else {
            for (Recommendation rec : topRecs) {

                ctx.append("Fund: ").append(rec.getFund().getFundName())
                        .append(" | Base: ").append(rec.getBaseMatchScore())
                        .append(" | Adjusted: ").append(rec.getMarketAdjustedScore());

                if (rec.getExplanationData() != null && !rec.getExplanationData().isEmpty()) {

                    Map<String, Object> exp = rec.getExplanationData();

                    ctx.append(" | Analysis: ");
                    ctx.append("ScoreDrop=")
                            .append(exp.getOrDefault("scoreDrop", "N/A")).append(", ");
                    ctx.append("Confidence=")
                            .append(exp.getOrDefault("confidenceScore", "N/A")).append("%, ");
                    ctx.append("RiskMatch=")
                            .append(exp.getOrDefault("riskMatch", "N/A"));
                }

                ctx.append("\n");
            }
        }

        ctx.append("\n=== MARKET EVENTS ===\n");

        for (MarketEvent ev : relevantEvents) {
            ctx.append(ev.getEventTitle())
                    .append(" (").append(ev.getImpactType()).append(")\n");
        }

        ctx.append("\n=== ALERTS ===\n");

        for (UserAlert alert : alerts) {
            ctx.append(alert.getAlertMessage()).append("\n");
        }

        ctx.append("\n=== HISTORY ===\n");

        for (ChatHistory h : recentHistory) {
            ctx.append("Q: ").append(h.getQuestion()).append("\n");
            ctx.append("A: ").append(h.getAnswer()).append("\n");
        }

        ctx.append("\n=== QUESTION ===\n");
        ctx.append(request.getQuestion());

        String ragContext = semanticSearchService.buildRagContext(request.getQuestion(), 4);
        String fullContext = ctx.toString() + "\n" + ragContext;

        String answer = investorChatbotAi.chat(fullContext);

        ChatHistory history = new ChatHistory();
        history.setUser(investor.getUser());
        history.setInvestor(investor);
        history.setQuestion(request.getQuestion());
        history.setAnswer(answer);
        history.setResponseType("CHAT");
        history.setModelName("llama-3.1-8b-instant");
        history.setAskedAt(now);

        chatHistoryRepository.save(history);

        return ChatResponseDto.builder()
                .investorId(investorId)
                .investorName(investor.getUser().getFullName())
                .question(request.getQuestion())
                .answer(answer)
                .responseType("CHAT")
                .modelName("llama-3.1-8b-instant")
                .askedAt(now)
                .build();
    }

    @Override
    public List<ChatResponseDto> getChatHistory(Long investorId) {

        investorProfileRepository.findById(investorId)
                .orElseThrow(() -> new InvestorNotFoundException(investorId));

        return chatHistoryRepository
                .findByInvestor_InvestorIdOrderByAskedAtDesc(investorId)
                .stream()
                .map(h -> ChatResponseDto.builder()
                        .investorId(investorId)
                        .investorName(h.getInvestor().getUser().getFullName())
                        .question(h.getQuestion())
                        .answer(h.getAnswer())
                        .responseType(h.getResponseType())
                        .modelName(h.getModelName())
                        .askedAt(h.getAskedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
