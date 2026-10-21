package com.roadguard.roadGuard_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpLoginRequestDto {
    @NotNull
    private String phoneNumber;

    @NotNull
    private String otp;
}
