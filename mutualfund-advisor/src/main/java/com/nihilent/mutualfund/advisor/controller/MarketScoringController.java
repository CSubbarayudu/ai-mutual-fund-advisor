package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.ScoringResultDto;
import com.nihilent.mutualfund.advisor.service.MarketScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/scoring")
@RequiredArgsConstructor
@Slf4j
public class MarketScoringController {

    private final MarketScoringService marketScoringService;

    @GetMapping("/investor/{investorId}")
    public ResponseEntity<List<ScoringResultDto>> scoreInvestor(@PathVariable Long investorId) {
        return ResponseEntity.ok(marketScoringService.scoreAllFundsForInvestor(investorId));
    }

    @GetMapping("/investor/{investorId}/fund/{fundId}")
    public ResponseEntity<ScoringResultDto> scoreOneFund(
            @PathVariable Long investorId, @PathVariable Long fundId) {
        return ResponseEntity.ok(marketScoringService.scoreOneFundForInvestor(investorId, fundId));
    }

    @PostMapping("/trigger-cycle")
    public ResponseEntity<String> triggerCycle() {
        marketScoringService.runFullMarketScoringCycle();
        return ResponseEntity.ok("Scoring cycle triggered successfully");
    }
}
