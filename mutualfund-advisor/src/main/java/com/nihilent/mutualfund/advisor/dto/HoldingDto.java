package com.nihilent.mutualfund.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingDto {

    private Long holdingId;
    private Long investorId;
    private Long fundId;
    private String fundName;
    private String holdingStatus;
    private LocalDateTime createdAt;
}
