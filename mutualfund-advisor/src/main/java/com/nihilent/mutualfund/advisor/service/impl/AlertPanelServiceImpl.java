package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.response.AlertResponseDto;
import com.nihilent.mutualfund.advisor.entity.UserAlert;
import com.nihilent.mutualfund.advisor.repository.UserAlertRepository;
import com.nihilent.mutualfund.advisor.repository.UserRepository;
import com.nihilent.mutualfund.advisor.service.AlertPanelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertPanelServiceImpl implements AlertPanelService {

    private final UserAlertRepository userAlertRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AlertResponseDto> getUnreadAlerts(Long userId) {
        verifyUser(userId);
        return userAlertRepository.findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertResponseDto> getAllAlerts(Long userId) {
        verifyUser(userId);
        return userAlertRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AlertResponseDto markAsRead(Long alertId, Long userId) {
        UserAlert alert = userAlertRepository.findByAlertIdAndUser_UserId(alertId, userId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        alert.setIsRead(true);
        userAlertRepository.save(alert);
        return mapToDto(alert);
    }

    private void verifyUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    private AlertResponseDto mapToDto(UserAlert a) {
        String fundName = (a.getRecommendation() != null && a.getRecommendation().getFund() != null)
                ? a.getRecommendation().getFund().getFundName() : null;
        String eventTitle = a.getEvent() != null ? a.getEvent().getEventTitle() : null;
        return AlertResponseDto.builder()
                .alertId(a.getAlertId())
                .userId(a.getUser().getUserId())
                .alertType(a.getAlertType())
                .alertMessage(a.getAlertMessage())
                .severity(a.getSeverity())
                .isRead(a.getIsRead())
                .createdAt(a.getCreatedAt())
                .fundName(fundName)
                .eventTitle(eventTitle)
                .build();
    }
}
