package com.roadguard.roadGuard_backend.dto;

import com.roadguard.roadGuard_backend.entity.types.HazardCategory;
import com.roadguard.roadGuard_backend.entity.types.Severity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectAiResponseDto {

    private String provider;

    private Integer aiPriorityScore;
    private Severity severity;
    private Double aiConfidence;

    private HazardCategory hazardCategory;

    private Boolean isRoadImage;
    private Boolean validHazard;
    private Boolean descriptionMatchesImage;
    private Boolean needsAdminReview;

    private String fakeLikelihood;
    private String explanation;
    private String rawResponse;
}
