package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.ChatRequestDto;
import com.nihilent.mutualfund.advisor.dto.ChatResponseDto;
import java.util.List;

public interface ChatbotService {
    ChatResponseDto chat(Long investorId, ChatRequestDto request);
    List<ChatResponseDto> getChatHistory(Long investorId);
}
