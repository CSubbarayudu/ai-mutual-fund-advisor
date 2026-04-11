package com.nihilent.mutualfund.advisor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponseDto {
    private Long alertId;
    private Long userId;
    private String alertType;
    private String alertMessage;
    private String severity;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String fundName;
    private String eventTitle;
}
