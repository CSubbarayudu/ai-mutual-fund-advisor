package com.nihilent.mutualfund.advisor.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RiskAssessmentRequest {

    private Long investorId;
    private List<AnswerDto> answers;

    @Data
    public static class AnswerDto {
        private Long questionId;
        private String selectedOption;
        private Integer optionScore;
    }
}
