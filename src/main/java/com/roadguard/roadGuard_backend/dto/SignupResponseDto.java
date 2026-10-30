package com.roadguard.roadGuard_backend.dto;

import com.roadguard.roadGuard_backend.entity.types.Roles;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.management.relation.Role;

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

    @NotBlank
    String token;

    @NotNull
    Roles role;
}
