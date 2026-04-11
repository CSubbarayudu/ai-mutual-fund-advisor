package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.response.AlertResponseDto;
import java.util.List;

public interface AlertPanelService {
    List<AlertResponseDto> getUnreadAlerts(Long userId);
    List<AlertResponseDto> getAllAlerts(Long userId);
    AlertResponseDto markAsRead(Long alertId, Long userId);
}
