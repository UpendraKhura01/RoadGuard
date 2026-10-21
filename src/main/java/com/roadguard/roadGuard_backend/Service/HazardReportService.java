package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.CreateHazardRequestDto;
import com.roadguard.roadGuard_backend.dto.HazardReportResponseDto;
import com.roadguard.roadGuard_backend.entity.HazardReport;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HazardReportService {

    public HazardReportResponseDto createReport(Long userid,
                                         CreateHazardRequestDto request,
                                         MultipartFile photo);


    HazardReportResponseDto getReportById(Long id);

    List<HazardReportResponseDto> getMyReports(Authentication authentication);

    List<HazardReportResponseDto> getVerifiedReports(Authentication authentication);

    List<HazardReportResponseDto> getReportsByStatus(ReportStatus status);

    List<HazardReportResponseDto> getAllReports();

    List<HazardReportResponseDto> getNearbyReports(Authentication authentication);

    List<HazardReportResponseDto> getCityReports(Authentication authentication);

    List<HazardReportResponseDto> getDistrictReports(Authentication authentication);

    List<HazardReportResponseDto> getStateReports(Authentication authentication);
}
