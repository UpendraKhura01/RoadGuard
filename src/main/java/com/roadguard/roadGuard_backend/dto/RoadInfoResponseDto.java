package com.roadguard.roadGuard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoadInfoResponseDto {
    private String name;

    private String contractorName;
    private String contractorContact;
    private String contractorEmail;

    private LocalDateTime lastMaintenance;
    private LocalDateTime warrantyTill;
    private BigDecimal budget;

    private Double latitude;
    private Double longitude;

}
