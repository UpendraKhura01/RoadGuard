package com.roadguard.roadGuard_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedBackRequestDto {

    @NotBlank(message = "messaged can't be blank")
    private String feedback;

    @NotNull(message = "reportId can't be null")
    private Long reportId;
    private int rating;
}
