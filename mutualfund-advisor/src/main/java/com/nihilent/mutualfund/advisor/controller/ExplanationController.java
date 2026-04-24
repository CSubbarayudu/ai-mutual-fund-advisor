package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.ApiResponse;
import com.nihilent.mutualfund.advisor.dto.ExplanationResponseDto;
import com.nihilent.mutualfund.advisor.service.ExplanationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/explain")
@RequiredArgsConstructor
@Slf4j
public class ExplanationController {

    private final ExplanationService explanationService;

    @PostMapping("/investor/{investorId}/fund/{fundId}")
    public ResponseEntity<ApiResponse<ExplanationResponseDto>> explain(
            @PathVariable Long investorId,
            @PathVariable Long fundId) {
        log.info("POST /api/v1/explain/investor/{}/fund/{}", investorId, fundId);
        ExplanationResponseDto result =
                explanationService.explainFundForInvestor(investorId, fundId);
        return ResponseEntity.ok(ApiResponse.success("Explanation generated", result));
    }
}
