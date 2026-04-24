package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.ApiResponse;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;

@CrossOrigin(origins = "*")

@RestController
@RequestMapping("/api/v1/scoring")
@RequiredArgsConstructor
@Slf4j
public class MarketScoringController {

    private final MarketScoringService marketScoringService;

    @GetMapping("/investor/{investorId}")
    public ResponseEntity<ApiResponse<List<ScoringResultDto>>> scoreInvestor(@PathVariable Long investorId) {
        List<ScoringResultDto> result = marketScoringService.scoreAllFundsForInvestor(investorId);
        return ResponseEntity.ok(ApiResponse.success("Scoring completed", result));
    }

    @GetMapping("/investor/{investorId}/fund/{fundId}")
    public ResponseEntity<ApiResponse<ScoringResultDto>> scoreOneFund(
            @PathVariable Long investorId, @PathVariable Long fundId) {
        ScoringResultDto result = marketScoringService.scoreOneFundForInvestor(investorId, fundId);
        return ResponseEntity.ok(ApiResponse.success("Fund scored", result));
    }

    @PostMapping("/trigger-cycle")
    public ResponseEntity<ApiResponse<String>> triggerCycle() {
        marketScoringService.runFullMarketScoringCycle();
        return ResponseEntity.ok(ApiResponse.success("Scoring cycle triggered successfully"));
    }
}