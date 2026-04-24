package com.nihilent.mutualfund.advisor.exception;

public class RecommendationNotFoundException extends RuntimeException {
    public RecommendationNotFoundException(Long investorId, Long fundId) {
        super("No recommendation found for investorId=" + investorId + ", fundId=" + fundId);
    }
}
