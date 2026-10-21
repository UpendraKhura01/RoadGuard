package com.roadguard.roadGuard_backend.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ContractorResponseDto {

    private String name;
    private String phoneNumber;
    private String email;
    private String area;
    private String specialization;
}
