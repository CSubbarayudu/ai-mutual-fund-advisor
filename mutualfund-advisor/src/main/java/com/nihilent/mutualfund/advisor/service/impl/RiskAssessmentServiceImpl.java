package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.request.RiskAssessmentRequest;
import com.nihilent.mutualfund.advisor.entity.InvestorProfile;
import com.nihilent.mutualfund.advisor.entity.RiskAssessment;
import com.nihilent.mutualfund.advisor.entity.RiskAssessmentAnswer;
import com.nihilent.mutualfund.advisor.entity.RiskQuestion;
import com.nihilent.mutualfund.advisor.repository.InvestorProfileRepository;
import com.nihilent.mutualfund.advisor.repository.RiskAssessmentAnswerRepository;
import com.nihilent.mutualfund.advisor.repository.RiskAssessmentRepository;
import com.nihilent.mutualfund.advisor.repository.RiskQuestionRepository;
import com.nihilent.mutualfund.advisor.service.RiskAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RiskAssessmentServiceImpl implements RiskAssessmentService {

    private final InvestorProfileRepository investorRepo;
    private final RiskQuestionRepository questionRepo;
    private final RiskAssessmentRepository assessmentRepo;
    private final RiskAssessmentAnswerRepository answerRepo;

    @Override
    public RiskAssessment submitAssessment(RiskAssessmentRequest request) {

        InvestorProfile investor = investorRepo.findById(request.getInvestorId())
                .orElseThrow(() -> new RuntimeException("Investor not found"));

        // 1. Calculate total score
        int totalScore = request.getAnswers()
                .stream()
                .mapToInt(a -> a.getOptionScore())
                .sum();

        // 2. Decide risk level
        String riskLevel = calculateRiskLevel(totalScore);

        // 3. Save assessment
        RiskAssessment assessment = new RiskAssessment();
        assessment.setInvestor(investor);
        assessment.setTotalScore(totalScore);
        assessment.setRiskLevel(riskLevel);
        assessment.setAssessedAt(LocalDateTime.now());
        assessment.setModelVersion("v1");

        assessment = assessmentRepo.save(assessment);

        // 4. Save answers
        for (RiskAssessmentRequest.AnswerDto dto : request.getAnswers()) {

            RiskQuestion question = questionRepo.findById(dto.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            RiskAssessmentAnswer answer = new RiskAssessmentAnswer();
            answer.setAssessment(assessment);
            answer.setQuestion(question);
            answer.setSelectedOption(dto.getSelectedOption());
            answer.setOptionScore(dto.getOptionScore());
            answer.setAnsweredAt(LocalDateTime.now());

            answerRepo.save(answer);
        }

        return assessment;
    }

    private String calculateRiskLevel(int score) {

        if (score <= 10) return "LOW";
        else if (score <= 25) return "MODERATE";
        else return "HIGH";
    }
}
