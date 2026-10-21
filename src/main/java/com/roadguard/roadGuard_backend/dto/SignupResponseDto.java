package com.roadguard.roadGuard_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupResponseDto {

    @NotNull
    Long userId;

    @NotBlank
    String username;

    @NotBlank
    String message;
}
