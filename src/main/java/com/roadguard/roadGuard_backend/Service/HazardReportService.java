package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.CreateHazardRequestDto;
import com.roadguard.roadGuard_backend.dto.HazardReportResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface HazardReportService {

    public HazardReportResponseDto createReport(Long userid,
                                         CreateHazardRequestDto request,
                                         MultipartFile photo);



}
