package com.roadguard.roadGuard_backend.Controller;

import com.roadguard.roadGuard_backend.Service.HazardReportService;
import com.roadguard.roadGuard_backend.dto.HazardReportResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hazard-report")
@RequiredArgsConstructor
public class HazardReportController {
    private final HazardReportService hazardReportService;

    public ResponseEntity<HazardReportResponseDto> createReport(){

    }
}
