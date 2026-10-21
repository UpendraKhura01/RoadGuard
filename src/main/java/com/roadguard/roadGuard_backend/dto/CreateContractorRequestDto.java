package com.roadguard.roadGuard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreateContractorRequestDto {

    private String name;
    private String phoneNumber;
    private String area;
    private String specialization;
}
