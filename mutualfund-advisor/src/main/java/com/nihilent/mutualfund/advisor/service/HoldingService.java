package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.HoldingDto;

import java.util.List;

public interface HoldingService {

    List<HoldingDto> getHoldingsByInvestor(Long investorId);
}