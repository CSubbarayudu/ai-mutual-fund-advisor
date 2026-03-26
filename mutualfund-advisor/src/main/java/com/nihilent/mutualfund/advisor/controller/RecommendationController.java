package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.entity.Recommendation;
import com.nihilent.mutualfund.advisor.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService service;

    @PostMapping("/{investorId}")
    public List<Recommendation> generate(@PathVariable Long investorId) {
        return service.generateRecommendations(investorId);
    }
}
