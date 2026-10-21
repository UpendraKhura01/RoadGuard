package com.roadguard.roadGuard_backend.dto;

import com.roadguard.roadGuard_backend.CustomAnnotation.PhoneNumber;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileRequestDto {

    private String name;

    @Email
    private String email;

    @PhoneNumber
    private String phoneNumber;
    private String otp;
}
