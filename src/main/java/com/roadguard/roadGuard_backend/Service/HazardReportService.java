package com.roadguard.roadGuard_backend.Service;

import com.roadguard.roadGuard_backend.dto.CreateHazardRequestDto;
import com.roadguard.roadGuard_backend.dto.HazardReportResponseDto;
import com.roadguard.roadGuard_backend.entity.User;
import com.roadguard.roadGuard_backend.entity.types.ReportStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HazardReportService {

    public HazardReportResponseDto createReport(Long userid,
                                         CreateHazardRequestDto request,
                                         MultipartFile photo);


    HazardReportResponseDto getReportById(Long id);

    List<HazardReportResponseDto> getMyReports(User user);

    List<HazardReportResponseDto> getVerifiedReports(User user);

    List<HazardReportResponseDto> getReportsByStatus(ReportStatus status);

    List<HazardReportResponseDto> getAllReports();

    List<HazardReportResponseDto> getNearbyReports(User user);

    List<HazardReportResponseDto> getCityReports(User user);

    List<HazardReportResponseDto> getDistrictReports(User user);

    List<HazardReportResponseDto> getStateReports(User user);
}
