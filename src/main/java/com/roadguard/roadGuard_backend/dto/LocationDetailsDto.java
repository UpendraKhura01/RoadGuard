package com.roadguard.roadGuard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationDetailsDto {
    private String road;
    private String ward;
    private String block;
    private String district;
    private String state;
    private String country;
    private String pinCode;
    private String fullAddress;

}
