package com.nihilent.mutualfund.advisor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestorProfileResponseDto {
    private Long investorId;
    private Long userId;
    private String fullName; // from investor.getUser().getFullName()
    private Integer age;
    private BigDecimal annualIncome;
    private String occupation;
    private String investmentGoal;
    private String investmentHorizon;
    private String liquidityPreference;
    private String investmentExperience;
    private LocalDateTime createdAt;
}
