package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.ApiResponse;
import com.nihilent.mutualfund.advisor.dto.HoldingDto;
import com.nihilent.mutualfund.advisor.service.HoldingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/holdings")
@RequiredArgsConstructor
@Slf4j
public class HoldingController {

    private final HoldingService holdingService;

    @GetMapping("/{investorId}")
    public ResponseEntity<ApiResponse<List<HoldingDto>>> getHoldingsByInvestor(@PathVariable Long investorId) {
        log.info("GET /api/v1/holdings/{}", investorId);
        return ResponseEntity.ok(ApiResponse.success("Holdings retrieved", holdingService.getHoldingsByInvestor(investorId)));
    }
}
