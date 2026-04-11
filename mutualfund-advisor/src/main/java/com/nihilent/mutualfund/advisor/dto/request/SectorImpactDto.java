package com.nihilent.mutualfund.advisor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectorImpactDto {
    @NotBlank(message = "Sector name is required")
    private String sectorName;
    @NotNull(message = "Impact severity is required")
    private BigDecimal impactSeverity; // maps to MarketEventSector.impactSeverity
}
