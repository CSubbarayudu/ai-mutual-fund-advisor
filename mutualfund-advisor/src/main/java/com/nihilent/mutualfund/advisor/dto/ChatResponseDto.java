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
public class ChatResponseDto {
    private Long investorId;
    private String investorName;
    private String question;
    private String answer;
    private String responseType;
    private String modelName;
    private LocalDateTime askedAt;
}
