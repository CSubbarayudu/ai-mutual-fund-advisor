package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.ApiResponse;
import com.nihilent.mutualfund.advisor.dto.RecommendationResponseDto;
import com.nihilent.mutualfund.advisor.entity.Recommendation;
import com.nihilent.mutualfund.advisor.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final RecommendationService service;

    @PostMapping("/{investorId}")
    public List<Recommendation> generate(@PathVariable Long investorId) {
        return service.generateRecommendations(investorId);
    }

    @GetMapping("/{investorId}")
    public ResponseEntity<ApiResponse<List<RecommendationResponseDto>>>
        getByPathVariable(@PathVariable Long investorId) {
        log.info("GET /api/v1/recommendations/{}", investorId);
        List<RecommendationResponseDto> result =
                service.getRecommendationsByInvestorId(investorId);
        return ResponseEntity.ok(ApiResponse.success("Recommendations fetched", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecommendationResponseDto>>>
        getByQueryParam(@RequestParam Long investorId) {
        log.info("GET /api/v1/recommendations?investorId={}", investorId);
        List<RecommendationResponseDto> result =
                service.getRecommendationsByInvestorId(investorId);
        return ResponseEntity.ok(ApiResponse.success("Recommendations fetched", result));
    }
}
