package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.ScoringResultDto;
import com.nihilent.mutualfund.advisor.dto.WhatIfRequestDto;

import java.util.List;

public interface WhatIfSimulationService {

    List<ScoringResultDto> simulate(Long investorId, WhatIfRequestDto overrides);
}