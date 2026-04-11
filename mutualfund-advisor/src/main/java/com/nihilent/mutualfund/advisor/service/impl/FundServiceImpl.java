package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.FundComparisonDto;
import com.nihilent.mutualfund.advisor.dto.FundSummaryDto;
import com.nihilent.mutualfund.advisor.entity.MutualFund;
import com.nihilent.mutualfund.advisor.exception.FundNotFoundException;
import com.nihilent.mutualfund.advisor.repository.MutualFundRepository;
import com.nihilent.mutualfund.advisor.service.FundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FundServiceImpl implements FundService {

    private final MutualFundRepository mutualFundRepository;

    @Override
    public List<FundSummaryDto> getAllActiveFunds() {
        log.info("Fetching all active mutual funds");
        return mutualFundRepository.findAll().stream()
                .filter(f -> "ACTIVE".equalsIgnoreCase(f.getFundStatus()))
                .map(this::toDto)
                .toList();
    }

    @Override
    public FundSummaryDto getFundById(Long fundId) {
        log.info("Fetching fund by id={}", fundId);
        MutualFund fund = mutualFundRepository.findById(fundId)
                .orElseThrow(() -> new FundNotFoundException(fundId));
        return toDto(fund);
    }

    @Override
    public FundComparisonDto compareFunds(Long fund1Id, Long fund2Id) {
        log.info("Comparing fund1={} vs fund2={}", fund1Id, fund2Id);
        MutualFund fund1 = mutualFundRepository.findById(fund1Id)
                .orElseThrow(() -> new FundNotFoundException(fund1Id));
        MutualFund fund2 = mutualFundRepository.findById(fund2Id)
                .orElseThrow(() -> new FundNotFoundException(fund2Id));

        FundSummaryDto dto1 = toDto(fund1);
        FundSummaryDto dto2 = toDto(fund2);

        String comparisonNote;
        if (fund1.getExpenseRatio() != null && fund2.getExpenseRatio() != null) {
            int cmp = fund1.getExpenseRatio().compareTo(fund2.getExpenseRatio());
            if (cmp < 0) {
                comparisonNote = "Fund 1 has lower expense ratio";
            } else if (cmp > 0) {
                comparisonNote = "Fund 2 has lower expense ratio";
            } else {
                comparisonNote = "Both funds have equal expense ratio";
            }
        } else {
            comparisonNote = "Expense ratio data unavailable for comparison";
        }

        return FundComparisonDto.builder()
                .fund1(dto1)
                .fund2(dto2)
                .comparisonNote(comparisonNote)
                .build();
    }

    private FundSummaryDto toDto(MutualFund fund) {
        return FundSummaryDto.builder()
                .fundId(fund.getFundId())
                .fundName(fund.getFundName())
                .amcName(fund.getAmcName())
                .category(fund.getCategory())
                .riskLevel(fund.getRiskLevel())
                .expenseRatio(fund.getExpenseRatio())
                .return1y(fund.getReturn1y())
                .return3y(fund.getReturn3y())
                .minimumInvestment(fund.getMinimumInvestment())
                .investmentHorizon(fund.getInvestmentHorizon())
                .fundStatus(fund.getFundStatus())
                .volatilityScore(fund.getVolatilityScore())
                .build();
    }
}