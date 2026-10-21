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
public class ImageUploadResponseDto {

    @NotBlank
    private String url;

    @NotBlank
    private String publicId;
}
