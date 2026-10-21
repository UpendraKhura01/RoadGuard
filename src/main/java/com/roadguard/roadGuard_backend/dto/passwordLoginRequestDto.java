package com.roadguard.roadGuard_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class passwordLoginRequestDto {

    @NotNull
    private String phoneNumber;

    @NotNull
    private String password;
}
