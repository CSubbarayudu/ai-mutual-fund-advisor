package com.nihilent.mutualfund.advisor.service;

import com.nihilent.mutualfund.advisor.dto.request.CreateUserRequest;
import com.nihilent.mutualfund.advisor.dto.request.CreateInvestorProfileRequest;
import com.nihilent.mutualfund.advisor.dto.response.UserResponseDto;
import com.nihilent.mutualfund.advisor.dto.response.InvestorProfileResponseDto;

public interface OnboardingService {
    UserResponseDto createUser(CreateUserRequest request);
    InvestorProfileResponseDto createInvestorProfile(CreateInvestorProfileRequest request);
    InvestorProfileResponseDto getInvestorProfile(Long investorId);
}
