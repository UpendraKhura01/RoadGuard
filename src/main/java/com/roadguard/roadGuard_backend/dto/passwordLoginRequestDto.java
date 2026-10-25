package com.roadguard.roadGuard_backend.dto;

import com.roadguard.roadGuard_backend.CustomAnnotation.PhoneNumber;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class passwordLoginRequestDto {

    @NotNull
    @PhoneNumber
    private String phoneNumber;

    @NotNull
    private String password;
}
