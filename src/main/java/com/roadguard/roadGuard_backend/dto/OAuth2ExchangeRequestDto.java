package com.roadguard.roadGuard_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuth2ExchangeRequestDto {

    @NotBlank(message = "code is required")
    private String code;
}
