package com.roadguard.roadGuard_backend.dto;

import com.roadguard.roadGuard_backend.CustomAnnotation.PhoneNumber;
import lombok.Data;

@Data
public class OtpRequestDto {

    @PhoneNumber
    String phoneNumber;
}
