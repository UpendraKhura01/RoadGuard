package com.roadguard.roadGuard_backend.dto;

import com.roadguard.roadGuard_backend.CustomAnnotation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class passwordLoginRequestDto {

    @PhoneNumber
    private String phoneNumber;


    @NotNull
    @NotBlank
    private String password;
}
