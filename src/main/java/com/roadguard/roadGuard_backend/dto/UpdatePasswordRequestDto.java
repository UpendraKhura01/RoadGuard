package com.roadguard.roadGuard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdatePasswordRequestDto {
    private String phoneNumber;
    private String otp;
    private String newPassword;

}
