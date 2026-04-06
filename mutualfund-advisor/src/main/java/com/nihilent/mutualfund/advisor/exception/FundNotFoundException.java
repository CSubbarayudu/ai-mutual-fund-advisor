package com.nihilent.mutualfund.advisor.exception;

public class FundNotFoundException extends RuntimeException {

    public FundNotFoundException(Long fundId) {
        super("Fund not found: " + fundId);
    }
}