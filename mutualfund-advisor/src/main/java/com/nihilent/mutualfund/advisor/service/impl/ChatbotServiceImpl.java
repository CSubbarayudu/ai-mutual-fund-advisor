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

    @Override
    @Transactional
    public ChatResponseDto chat(Long investorId, ChatRequestDto request) {
        log.info("Chatbot request for investorId={} question='{}'",
                investorId, request.getQuestion());

        // Step 1: Validate investor
        InvestorProfile investor = investorProfileRepository.findById(investorId)
                .orElseThrow(() -> new InvestorNotFoundException(investorId));

        // Step 2: Resolve userId — UserAlert is user-owned, not investor-owned
        Long userId = investor.getUser().getUserId();

        // Step 3: Get risk level
        String riskLevel = riskAssessmentRepository
                .findTopByInvestor_InvestorIdOrderByAssessedAtDesc(investorId)
                .map(RiskAssessment::getRiskLevel)
                .orElse("NOT ASSESSED");

        // Step 4: Get top 3 recommendations by market-adjusted score
        List<Recommendation> topRecs = recommendationRepository
                .findByInvestor_InvestorIdOrderByMarketAdjustedScoreDesc(investorId)
                .stream()
                .limit(3)
                .collect(Collectors.toList());

        // Step 5: Get up to 3 unread active alerts via userId (correct relation)
        List<UserAlert> alerts = userAlertRepository
                .findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .limit(3)
                .collect(Collectors.toList());

        // Step 6: Get last 5 chat messages for conversation context
        List<ChatHistory> recentHistory = chatHistoryRepository
                .findByInvestor_InvestorIdOrderByAskedAtDesc(investorId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        // Step 7: Build full context string
        LocalDateTime now = LocalDateTime.now();
        StringBuilder ctx = new StringBuilder();

        ctx.append("=== INVESTOR PROFILE ===\n");
        ctx.append("Name: ").append(investor.getUser().getFullName()).append("\n");
        ctx.append("Risk Level: ").append(riskLevel).append("\n");
        ctx.append("Investment Goal: ").append(investor.getInvestmentGoal()).append("\n");
        ctx.append("Investment Horizon: ").append(investor.getInvestmentHorizon()).append("\n\n");

        ctx.append("=== TOP FUND RECOMMENDATIONS (by market-adjusted score) ===\n");
        if (topRecs.isEmpty()) {
            ctx.append("No recommendations yet. Run POST /api/v1/scoring/trigger-cycle first.\n");
        } else {
            for (Recommendation rec : topRecs) {
                ctx.append("Fund: ").append(rec.getFund().getFundName())
                   .append(" | Category: ").append(rec.getFund().getCategory())
                   .append(" | Fund Risk: ").append(rec.getFund().getRiskLevel())
                   .append(" | Base Score: ").append(rec.getBaseMatchScore())
                   .append("/100 | Market-Adjusted Score: ")
                   .append(rec.getMarketAdjustedScore()).append("/100\n");
            }
        }
        ctx.append("\n");

        ctx.append("=== ACTIVE MARKET ALERTS ===\n");
        if (alerts.isEmpty()) {
            ctx.append("No active unread alerts.\n");
        } else {
            for (UserAlert alert : alerts) {
                ctx.append("- ").append(alert.getAlertMessage())
                   .append(" [Severity: ").append(alert.getSeverity()).append("]\n");
            }
        }
        ctx.append("\n");

        ctx.append("=== RECENT CONVERSATION HISTORY ===\n");
        if (recentHistory.isEmpty()) {
            ctx.append("No prior conversation.\n");
        } else {
            for (ChatHistory h : recentHistory) {
                ctx.append("Q: ").append(h.getQuestion()).append("\n");
                ctx.append("A: ").append(h.getAnswer()).append("\n\n");
            }
        }

        ctx.append("=== CURRENT QUESTION ===\n");
        ctx.append(request.getQuestion());

        log.debug("AI context built for investorId={}: {} chars", investorId, ctx.length());

        // Step 8: Call AI
        String answer = investorChatbotAi.chat(ctx.toString());
        log.info("AI chatbot response generated for investorId={}", investorId);

        // Step 9: Save to chat_history
        ChatHistory history = new ChatHistory();
        history.setUser(investor.getUser());
        history.setInvestor(investor);
        history.setQuestion(request.getQuestion());
        history.setAnswer(answer);
        history.setResponseType("CHAT");
        history.setModelName("llama-3.1-8b-instant");
        history.setAskedAt(now);
        chatHistoryRepository.save(history);

        // Step 10: Return DTO
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
