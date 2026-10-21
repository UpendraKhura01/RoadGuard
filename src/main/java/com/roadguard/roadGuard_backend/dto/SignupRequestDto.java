package com.roadguard.roadGuard_backend.dto;

import com.roadguard.roadGuard_backend.CustomAnnotation.PhoneNumber;
import com.roadguard.roadGuard_backend.entity.types.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignupRequestDto {

    @NotNull
    @PhoneNumber
    String phoneNumber;

    @NotBlank
    String name;

    @NotNull
    String password;

    AuthProvider authProvider;

    @NotNull
    String otp;
}
