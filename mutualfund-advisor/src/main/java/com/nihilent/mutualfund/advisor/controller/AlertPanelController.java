package com.nihilent.mutualfund.advisor.controller;

import com.nihilent.mutualfund.advisor.dto.ApiResponse;
import com.nihilent.mutualfund.advisor.service.AlertPanelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertPanelController {

    private final AlertPanelService alertPanelService;

    @GetMapping("/users/{userId}/unread")
    public ResponseEntity<ApiResponse<Object>> getUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Unread alerts fetched", alertPanelService.getUnreadAlerts(userId)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Object>> getAll(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("All alerts fetched", alertPanelService.getAllAlerts(userId)));
    }

    @PatchMapping("/{alertId}/users/{userId}/mark-read")
    public ResponseEntity<ApiResponse<Object>> markRead(@PathVariable Long alertId, @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Alert marked as read", alertPanelService.markAsRead(alertId, userId)));
    }
}
