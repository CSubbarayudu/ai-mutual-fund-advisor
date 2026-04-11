package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.ApiResponse;
import com.nihilent.mutualfund.advisor.dto.request.CreateInvestorProfileRequest;
import com.nihilent.mutualfund.advisor.dto.request.CreateUserRequest;
import com.nihilent.mutualfund.advisor.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
@Slf4j
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<Object>> createUser(@Valid @RequestBody CreateUserRequest r) {
        return ResponseEntity.ok(ApiResponse.success("User registered", onboardingService.createUser(r)));
    }

    @PostMapping("/investor-profiles")
    public ResponseEntity<ApiResponse<Object>> createInvestorProfile(@Valid @RequestBody CreateInvestorProfileRequest r) {
        return ResponseEntity.ok(ApiResponse.success("Investor profile created", onboardingService.createInvestorProfile(r)));
    }

    @GetMapping("/investor-profiles/{investorId}")
    public ResponseEntity<ApiResponse<Object>> getInvestorProfile(@PathVariable Long investorId) {
        return ResponseEntity.ok(ApiResponse.success("Investor profile fetched", onboardingService.getInvestorProfile(investorId)));
    }
}
