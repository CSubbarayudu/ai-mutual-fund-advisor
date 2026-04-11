package com.nihilent.mutualfund.advisor.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketEventIngestRequest {
    @NotBlank(message = "Event title is required")
    private String eventTitle;
    private String eventDescription;
    @NotBlank(message = "Impact type is required")
    private String impactType;            // POSITIVE / NEGATIVE / NEUTRAL
    private String impactDuration;        // String e.g. "90 days" — matches entity field
    private BigDecimal credibilityScore;
    private String sourceUrl;
    @NotNull(message = "Event date is required")
    private LocalDateTime eventDate;
    @NotNull(message = "Expiry date is required")
    private LocalDateTime expiryDate;
    @NotEmpty(message = "At least one affected sector is required")
    @Valid
    private List<SectorImpactDto> affectedSectors;
}
