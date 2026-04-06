package com.nihilent.mutualfund.advisor.exception;

public class RiskAssessmentNotFoundException extends RuntimeException {

    public RiskAssessmentNotFoundException(Long investorId) {
        super("No risk assessment found for investor: " + investorId);
    }
}