package com.roadguard.roadGuard_backend.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AssignContractorDto {
    private Long reportId;
    private Long contractorId;
}
