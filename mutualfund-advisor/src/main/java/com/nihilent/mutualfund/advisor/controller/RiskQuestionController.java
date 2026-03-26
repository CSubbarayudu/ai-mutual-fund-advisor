package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.entity.RiskQuestion;
import com.nihilent.mutualfund.advisor.repository.RiskQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/risk/questions")
@RequiredArgsConstructor
public class RiskQuestionController {

    private final RiskQuestionRepository repository;

    @GetMapping
    public List<RiskQuestion> getAllQuestions() {
        return repository.findAll();
    }
}
