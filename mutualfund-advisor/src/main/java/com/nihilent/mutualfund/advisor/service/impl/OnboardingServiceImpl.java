package com.nihilent.mutualfund.advisor.service.impl;

import com.nihilent.mutualfund.advisor.dto.request.CreateInvestorProfileRequest;
import com.nihilent.mutualfund.advisor.dto.request.CreateUserRequest;
import com.nihilent.mutualfund.advisor.dto.response.InvestorProfileResponseDto;
import com.nihilent.mutualfund.advisor.dto.response.UserResponseDto;
import com.nihilent.mutualfund.advisor.entity.InvestorProfile;
import com.nihilent.mutualfund.advisor.entity.Users;
import com.nihilent.mutualfund.advisor.exception.InvestorNotFoundException;
import com.nihilent.mutualfund.advisor.repository.InvestorProfileRepository;
import com.nihilent.mutualfund.advisor.repository.UserRepository;
import com.nihilent.mutualfund.advisor.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OnboardingServiceImpl implements OnboardingService {

    private final UserRepository userRepository;
    private final InvestorProfileRepository investorProfileRepository;

    @Override
    public UserResponseDto createUser(CreateUserRequest request) {
        Users user = new Users();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        user.setRole(request.getRole() != null ? request.getRole() : "INVESTOR");
        user.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        Users saved = userRepository.save(user);
        log.info("User created: userId={}", saved.getUserId());
        return UserResponseDto.builder()
                .userId(saved.getUserId()).fullName(saved.getFullName())
                .email(saved.getEmail()).mobile(saved.getMobile())
                .role(saved.getRole()).status(saved.getStatus())
                .createdAt(saved.getCreatedAt()).build();
    }

    @Override
    public InvestorProfileResponseDto createInvestorProfile(CreateInvestorProfileRequest request) {
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getUserId()));
        InvestorProfile p = new InvestorProfile();
        p.setUser(user);
        p.setAge(request.getAge());
        p.setAnnualIncome(request.getAnnualIncome());
        p.setOccupation(request.getOccupation());
        p.setInvestmentGoal(request.getInvestmentGoal());
        p.setInvestmentHorizon(request.getInvestmentHorizon());
        p.setLiquidityPreference(request.getLiquidityPreference());
        p.setInvestmentExperience(request.getInvestmentExperience());
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        InvestorProfile saved = investorProfileRepository.save(p);
        log.info("InvestorProfile created: investorId={}", saved.getInvestorId());
        return mapToProfileDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InvestorProfileResponseDto getInvestorProfile(Long investorId) {
        return mapToProfileDto(investorProfileRepository.findById(investorId)
                .orElseThrow(() -> new InvestorNotFoundException(investorId)));
    }

    private InvestorProfileResponseDto mapToProfileDto(InvestorProfile p) {
        return InvestorProfileResponseDto.builder()
                .investorId(p.getInvestorId()).userId(p.getUser().getUserId())
                .fullName(p.getUser().getFullName()).age(p.getAge())
                .annualIncome(p.getAnnualIncome()).occupation(p.getOccupation())
                .investmentGoal(p.getInvestmentGoal()).investmentHorizon(p.getInvestmentHorizon())
                .liquidityPreference(p.getLiquidityPreference())
                .investmentExperience(p.getInvestmentExperience())
                .createdAt(p.getCreatedAt()).build();
    }
}
