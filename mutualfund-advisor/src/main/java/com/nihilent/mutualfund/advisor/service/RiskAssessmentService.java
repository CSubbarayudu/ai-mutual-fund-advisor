package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.request.RiskAssessmentRequest;
import com.nihilent.mutualfund.advisor.entity.RiskAssessment;

public interface RiskAssessmentService {

    RiskAssessment submitAssessment(RiskAssessmentRequest request);
}