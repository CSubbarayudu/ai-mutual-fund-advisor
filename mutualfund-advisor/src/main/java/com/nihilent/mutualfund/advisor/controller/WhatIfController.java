package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.ApiResponse;
import com.nihilent.mutualfund.advisor.dto.ScoringResultDto;
import com.nihilent.mutualfund.advisor.dto.WhatIfRequestDto;
import com.nihilent.mutualfund.advisor.service.WhatIfSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/simulation")
@RequiredArgsConstructor
@Slf4j
public class WhatIfController {

    private final WhatIfSimulationService whatIfSimulationService;

    @PostMapping("/what-if/{investorId}")
    public ResponseEntity<ApiResponse<List<ScoringResultDto>>> simulate(
            @PathVariable Long investorId,
            @RequestBody WhatIfRequestDto overrides) {
        log.info("POST /api/v1/simulation/what-if/{} overrides={}", investorId, overrides);
        List<ScoringResultDto> result = whatIfSimulationService.simulate(investorId, overrides);
        return ResponseEntity.ok(ApiResponse.success("Simulation complete", result));
    }
}

