package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.ApiResponse;
import com.nihilent.mutualfund.advisor.dto.ChatRequestDto;
import com.nihilent.mutualfund.advisor.dto.ChatResponseDto;
import com.nihilent.mutualfund.advisor.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/investor/{investorId}")
    public ResponseEntity<ApiResponse<ChatResponseDto>> chat(
            @PathVariable Long investorId,
            @Valid @RequestBody ChatRequestDto request) {
        log.info("POST /api/v1/chat/investor/{}", investorId);
        ChatResponseDto result = chatbotService.chat(investorId, request);
        return ResponseEntity.ok(ApiResponse.success("Chat response generated", result));
    }

    @GetMapping("/investor/{investorId}/history")
    public ResponseEntity<ApiResponse<List<ChatResponseDto>>> getHistory(
            @PathVariable Long investorId) {
        log.info("GET /api/v1/chat/investor/{}/history", investorId);
        List<ChatResponseDto> history = chatbotService.getChatHistory(investorId);
        return ResponseEntity.ok(ApiResponse.success("Chat history retrieved", history));
    }
}
