package com.roadguard.roadGuard_backend.dto;

import com.roadguard.roadGuard_backend.entity.types.HazardCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateHazardRequestDto {

    private String description;

    private HazardCategory reportedCategory;

    private Double longitude;
    private Double latitude;
    private Double gpsAccuracy;// range 0 - 500 meters

    private String address;
    private String city;
    private String ward;

    private Boolean cameraOnly;
    private Boolean vpnDetected;
    private Boolean fakeLocation;

    private LocalDateTime capturedAt;

    private MultipartFile photo;
}
