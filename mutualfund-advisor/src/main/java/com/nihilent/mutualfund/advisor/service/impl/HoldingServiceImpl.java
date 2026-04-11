package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.HoldingDto;
import com.nihilent.mutualfund.advisor.exception.InvestorNotFoundException;
import com.nihilent.mutualfund.advisor.repository.InvestorHoldingRepository;
import com.nihilent.mutualfund.advisor.repository.InvestorProfileRepository;
import com.nihilent.mutualfund.advisor.service.HoldingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HoldingServiceImpl implements HoldingService {

    private final InvestorProfileRepository investorProfileRepository;
    private final InvestorHoldingRepository investorHoldingRepository;

    @Override
    public List<HoldingDto> getHoldingsByInvestor(Long investorId) {
        log.info("Fetching holdings for investorId={}", investorId);

        investorProfileRepository.findById(investorId)
                .orElseThrow(() -> new InvestorNotFoundException(investorId));

        return investorHoldingRepository.findByInvestor_InvestorId(investorId).stream()
                .map(holding -> HoldingDto.builder()
                        .holdingId(holding.getHoldingId())
                        .investorId(holding.getInvestor().getInvestorId())
                        .fundId(holding.getFund().getFundId())
                        .fundName(holding.getFund().getFundName())
                        .holdingStatus(holding.getHoldingStatus())
                        .createdAt(holding.getCreatedAt())
                        .build())
                .toList();
    }
}

