package com.roadguard.roadGuard_backend.dto;

import com.roadguard.roadGuard_backend.CustomAnnotation.PhoneNumber;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpLoginRequestDto {
    @NotNull
    @PhoneNumber
    private String phoneNumber;

    @NotNull
    private String otp;
}
