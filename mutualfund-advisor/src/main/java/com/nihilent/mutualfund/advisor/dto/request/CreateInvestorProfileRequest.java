package com.nihilent.mutualfund.advisor.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvestorProfileRequest {
    @NotNull(message = "userId is required") private Long userId;
    @NotNull(message = "age is required") private Integer age;
    private BigDecimal annualIncome;
    private String occupation;
    private String investmentGoal;
    private String investmentHorizon;
    private String liquidityPreference;
    private String investmentExperience;
}
