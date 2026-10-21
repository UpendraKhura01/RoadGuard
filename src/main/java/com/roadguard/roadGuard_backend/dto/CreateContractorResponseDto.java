package com.roadguard.roadGuard_backend.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CreateContractorResponseDto {

    private Long id;
    private String name;
    private String phoneNumber;
    private String area;
    private String specialization;
}
