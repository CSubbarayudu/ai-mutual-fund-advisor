package com.nihilent.mutualfund.advisor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long userId;
    private String fullName;
    private String email;
    private String mobile;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}
