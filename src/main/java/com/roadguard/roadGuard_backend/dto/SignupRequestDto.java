package com.roadguard.roadGuard_backend.dto;

import com.roadguard.roadGuard_backend.CustomAnnotation.PhoneNumber;
import com.roadguard.roadGuard_backend.entity.types.AuthProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    @Email
    String email;
}
