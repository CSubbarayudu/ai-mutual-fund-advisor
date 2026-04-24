package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.request.RiskAssessmentRequest;
import com.nihilent.mutualfund.advisor.entity.RiskAssessment;
import com.nihilent.mutualfund.advisor.service.RiskAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
@CrossOrigin(origins = "*")

@RestController
@RequestMapping("/api/v1/risk/assessments")
@RequiredArgsConstructor
public class RiskAssessmentController {

    private final RiskAssessmentService service;

    @PostMapping
    public RiskAssessment submit(@RequestBody RiskAssessmentRequest request) {
        return service.submitAssessment(request);
    }
}
