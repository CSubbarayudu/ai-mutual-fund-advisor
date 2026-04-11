package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.ApiResponse;
import com.nihilent.mutualfund.advisor.dto.FundComparisonDto;
import com.nihilent.mutualfund.advisor.dto.FundSummaryDto;
import com.nihilent.mutualfund.advisor.service.FundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/funds")
@RequiredArgsConstructor
@Slf4j
public class FundController {

    private final FundService fundService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FundSummaryDto>>> getAllActiveFunds() {
        log.info("GET /api/v1/funds");
        return ResponseEntity.ok(ApiResponse.success("Active funds retrieved", fundService.getAllActiveFunds()));
    }

    @GetMapping("/{fundId}")
    public ResponseEntity<ApiResponse<FundSummaryDto>> getFundById(@PathVariable Long fundId) {
        log.info("GET /api/v1/funds/{}", fundId);
        return ResponseEntity.ok(ApiResponse.success("Fund retrieved", fundService.getFundById(fundId)));
    }

    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<FundComparisonDto>> compareFunds(
            @RequestParam Long fund1,
            @RequestParam Long fund2) {
        log.info("GET /api/v1/funds/compare?fund1={}&fund2={}", fund1, fund2);
        return ResponseEntity.ok(ApiResponse.success("Comparison complete", fundService.compareFunds(fund1, fund2)));
    }
}
