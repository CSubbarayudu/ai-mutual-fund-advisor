package com.nihilent.mutualfund.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WhatIfRequestDto {

    private String riskLevel;
    private String investmentHorizon;
}
