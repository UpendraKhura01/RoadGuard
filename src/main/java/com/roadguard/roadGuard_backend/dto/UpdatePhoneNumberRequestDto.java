package com.roadguard.roadGuard_backend.dto;

import com.roadguard.roadGuard_backend.CustomAnnotation.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePhoneNumberRequestDto {

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String oldPhoneNumberOtp;

    @NotBlank
    private String newPhoneNumberOtp;

}
