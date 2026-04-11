package com.nihilent.mutualfund.advisor.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface FundAdvisorAi {

    @SystemMessage("""
        You are an expert mutual fund advisor AI.
        Explain investment decisions in simple, clear English.
        Be concise (3-5 sentences). Use plain language, no jargon.
        Focus on WHY the score changed due to market events.
        Always end with one actionable sentence for the investor.
        """)
    String explainFundScore(@UserMessage String context);
}
